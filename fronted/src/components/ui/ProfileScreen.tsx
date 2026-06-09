import { useState, useEffect, useRef } from "react";
import { motion, AnimatePresence } from "framer-motion";
import {
  ChevronLeft,
  Pencil,
  Check,
  Loader2,
  User,
  Heart,
  Sparkles,
  Smile,
  Tag,
  Camera,
} from "lucide-react";
import { useAuthStore } from "../../stores/authStore";
import { fetchProfile, updateProfile as updateProfileApi } from "../../services/profileService";

interface Props {
  onBack: () => void;
}

const HOBBY_SUGGESTIONS = [
  "Sport", "Musik", "Lesen", "Kochen", "Reisen",
  "Fotografie", "Zeichnen", "Gaming", "Programmieren",
  "Malen", "Tanzen", "Filme",
];

const INTEREST_SUGGESTIONS = [
  "Natur", "Technologie", "Kunst", "Wissenschaft",
  "Architektur", "Literatur", "Fitness", "Kaffee",
  "Music", "Dramafestivals",
];

const BUBBLE_COLORS = [
  "#F97316", "#8B5CF6", "#EC4899", "#3B82F6",
  "#10B981", "#F59E0B", "#6366F1", "#14B8A6",
  "#EF4444", "#84CC16", "#A855F7", "#06B6D4",
];

export function ProfileScreen({ onBack }: Props) {
  const { token, user, updateProfile: storeUpdate } = useAuthStore();
  const fileRef = useRef<HTMLInputElement>(null);

  const [displayName, setDisplayName] = useState("");
  const [bio, setBio] = useState("");
  const [hobbies, setHobbies] = useState<string[]>([]);
  const [likes, setLikes] = useState<string[]>([]);
  const [avatarFile, setAvatarFile] = useState<File | null>(null);
  const [avatarPreview, setAvatarPreview] = useState<string | null>(null);

  const [newHobby, setNewHobby] = useState("");
  const [newLike, setNewLike] = useState("");

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);
  const [fetchError, setFetchError] = useState(false);

  useEffect(() => {
    if (!token) return;
    (async () => {
      try {
        const profile = await fetchProfile(token);
        setDisplayName(profile.displayName || "");
        setBio(profile.bio || "");
        setHobbies(user?.hobbies || []);
        setLikes(user?.likes || []);
      } catch {
        setFetchError(true);
      } finally {
        setLoading(false);
      }
    })();
  }, [token]);

  useEffect(() => {
    if (saved) {
      const t = setTimeout(() => setSaved(false), 2000);
      return () => clearTimeout(t);
    }
  }, [saved]);

  function handleAvatarSelect(e: React.ChangeEvent<HTMLInputElement>) {
    const f = e.target.files?.[0];
    if (!f) return;
    setAvatarFile(f);
    setAvatarPreview(URL.createObjectURL(f));
  }

  async function handleSave() {
    if (!token || saving) return;
    setSaving(true);
    try {
      const result = await updateProfileApi(token, {
        displayName,
        bio,
        profileImage: avatarFile,
      });

      storeUpdate({
        displayName: result.displayName,
        bio: result.bio,
        profileImageUrl: result.profileImageUrl,
        hobbies,
        likes,
      });

      setAvatarFile(null);
      setSaved(true);
    } catch {
      /* retry */
    } finally {
      setSaving(false);
    }
  }

  function addHobby(value: string) {
    if (value.trim() && !hobbies.includes(value.trim())) {
      setHobbies((p) => [...p, value.trim()]);
    }
    setNewHobby("");
  }

  function addLike(value: string) {
    if (value.trim() && !likes.includes(value.trim())) {
      setLikes((p) => [...p, value.trim()]);
    }
    setNewLike("");
  }

  const inputBase =
    "bg-white rounded-full px-5 py-3.5 shadow-inner flex items-center gap-3 w-full";
  const labelBase = "text-[10px] text-gray-400 font-bold leading-none mb-0.5";

  if (loading) {
    return (
      <div className="flex min-h-screen w-full bg-gradient-to-br from-[#F97316] to-[#FDBA74] justify-center items-center">
        <Loader2 size={32} className="text-white animate-spin" />
      </div>
    );
  }

  if (fetchError) {
    return (
      <div className="flex min-h-screen w-full bg-gradient-to-br from-[#F97316] to-[#FDBA74] justify-center items-center px-4">
        <div className="bg-white/20 rounded-2xl p-6 text-center max-w-sm">
          <p className="text-white font-bold text-lg mb-2">Verbindungsfehler</p>
          <p className="text-white/80 text-sm mb-4">Profil konnte nicht geladen werden.</p>
          <button onClick={onBack} className="bg-white text-[#EA580C] font-bold px-6 py-2.5 rounded-full text-sm cursor-pointer">
            Zurück
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="flex min-h-screen w-full bg-gradient-to-br from-[#F97316] to-[#FDBA74] justify-center items-start px-4 py-8 relative overflow-y-auto">
      <AnimatePresence>
        {saved && (
          <motion.div
            initial={{ opacity: 0, y: -30 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -30 }}
            className="fixed top-8 left-1/2 -translate-x-1/2 z-50 flex items-center gap-2 bg-emerald-600 text-white rounded-full px-5 py-2.5 shadow-lg"
          >
            <Check size={16} />
            <span className="text-[13px] font-bold">Erfolgreich gespeichert!</span>
          </motion.div>
        )}
      </AnimatePresence>

      <div className="absolute top-6 left-6 z-10">
        <button onClick={onBack} className="flex items-center gap-1.5 text-white font-semibold cursor-pointer transition-opacity hover:opacity-80">
          <ChevronLeft size={20} />
          <span className="text-[14px]">Zurück</span>
        </button>
      </div>

      <motion.div
        initial={{ scale: 0.95, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        transition={{ duration: 0.35 }}
        className="w-full max-w-[420px] bg-[#EA580C] rounded-[40px] shadow-[0_25px_50px_-12px_rgba(0,0,0,0.4)] overflow-hidden pb-10 flex flex-col items-center"
      >
        <div className="w-[140%] bg-white rounded-b-[100%] flex justify-center items-center pt-12 pb-8 shadow-sm mb-6 -mt-2">
          <div className="text-center">
            <div className="relative w-20 h-20 mx-auto mb-3">
              <div className="w-20 h-20 bg-gradient-to-br from-[#F97316] to-[#FDBA74] rounded-full flex items-center justify-center shadow-md overflow-hidden">
                {avatarPreview ? (
                  <img src={avatarPreview} alt="" className="w-full h-full object-cover" />
                ) : user?.profileImageUrl ? (
                  <img src={user.profileImageUrl} alt="" className="w-full h-full object-cover" />
                ) : (
                  <User size={32} className="text-white" />
                )}
              </div>
              <button
                onClick={() => fileRef.current?.click()}
                type="button"
                className="absolute -bottom-1 -right-1 w-7 h-7 bg-white rounded-full flex items-center justify-center shadow-md hover:scale-105 transition-transform cursor-pointer"
              >
                <Camera size={14} className="text-[#EA580C]" />
              </button>
              <input
                ref={fileRef}
                type="file"
                accept="image/*"
                onChange={handleAvatarSelect}
                className="hidden"
              />
            </div>
            <h1 className="text-[26px] font-extrabold text-[#2A1405] leading-tight">
              Profil bearbeiten
            </h1>
            <p className="text-gray-400 text-[13px] font-medium mt-0.5">
              {displayName || "Dein Name"}
            </p>
          </div>
        </div>

        <div className="w-full px-8 space-y-4 pb-6">
          <div className={inputBase}>
            <User size={18} className="text-[#EA580C] flex-shrink-0" />
            <div className="flex-1 flex flex-col">
              <span className={labelBase}>Anzeigename</span>
              <input
                type="text"
                value={displayName}
                onChange={(e) => setDisplayName(e.target.value)}
                placeholder="Wie soll man dich nennen?"
                className="bg-transparent text-[14px] text-[#2D1A05] outline-none font-semibold placeholder-gray-300 w-full"
              />
            </div>
          </div>

          <div className={`${inputBase} opacity-60`}>
            <User size={18} className="text-[#EA580C] flex-shrink-0" />
            <div className="flex-1 flex flex-col">
              <span className={labelBase}>E-Mail Adresse</span>
              <input
                type="email"
                readOnly
                disabled
                value={user?.universityEmail ?? ""}
                placeholder="—@stud.fh-dortmund.de"
                className="bg-transparent text-[14px] text-[#2D1A05] outline-none font-semibold w-full cursor-not-allowed"
              />
            </div>
          </div>

          <div className="bg-white rounded-2xl px-5 py-3.5 shadow-inner flex items-start gap-3">
            <Pencil size={18} className="text-[#EA580C] flex-shrink-0 mt-0.5" />
            <div className="flex-1 flex flex-col">
              <span className={labelBase}>Bio / Beschreibung</span>
              <textarea
                value={bio}
                onChange={(e) => setBio(e.target.value)}
                placeholder="Erzähle etwas über dich..."
                rows={3}
                className="bg-transparent text-[14px] text-[#2D1A05] outline-none font-semibold placeholder-gray-300 w-full resize-none"
              />
            </div>
          </div>

          <div className="border-t border-white/20 my-1" />

          <section>
            <div className="flex items-center gap-2 mb-3">
              <Tag size={14} className="text-white/70" />
              <span className="text-[11px] text-white font-bold tracking-wide uppercase">
                Hobbies
              </span>
            </div>

            <div className="flex flex-wrap gap-2 mb-3">
              {HOBBY_SUGGESTIONS.map((h) => (
                <motion.button
                  key={h}
                  whileTap={{ scale: 0.92 }}
                  onClick={() =>
                    hobbies.includes(h)
                      ? setHobbies((p) => p.filter((x) => x !== h))
                      : setHobbies((p) => [...p, h])
                  }
                  type="button"
                  className={`px-3 py-1.5 rounded-full text-[11px] font-bold transition-colors ${
                    hobbies.includes(h)
                      ? "bg-white text-[#EA580C]"
                      : "bg-white/20 text-white hover:bg-white/30"
                  }`}
                >
                  {h}
                </motion.button>
              ))}
            </div>

            <div className="flex gap-2">
              <div className="flex-1 bg-white rounded-full px-4 py-3 shadow-inner flex items-center gap-2">
                <Sparkles size={16} className="text-[#EA580C] flex-shrink-0" />
                <input
                  type="text"
                  value={newHobby}
                  onChange={(e) => setNewHobby(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === "Enter") { e.preventDefault(); addHobby(newHobby); }
                  }}
                  placeholder="Eigenes Hobby…"
                  className="bg-transparent text-[13px] text-[#2D1A05] outline-none font-semibold placeholder-gray-300 w-full"
                />
              </div>
              <button
                onClick={() => addHobby(newHobby)}
                type="button"
                className="bg-white rounded-full w-11 h-11 flex items-center justify-center shadow-inner flex-shrink-0 hover:scale-105 transition-transform cursor-pointer"
              >
                <Sparkles size={16} className="text-[#EA580C]" />
              </button>
            </div>

            <AnimatePresence mode="popLayout">
              {hobbies.length > 0 && (
                <motion.div className="flex flex-wrap gap-2 mt-3" layout>
                  {hobbies.map((h, i) => (
                    <motion.span
                      key={h}
                      initial={{ scale: 0, opacity: 0 }}
                      animate={{ scale: 1, opacity: 1 }}
                      exit={{ scale: 0, opacity: 0 }}
                      layout
                      style={{ backgroundColor: BUBBLE_COLORS[i % BUBBLE_COLORS.length] }}
                      className="inline-flex items-center gap-1.5 text-white text-[12px] font-bold px-3.5 py-1.5 rounded-full shadow-sm"
                    >
                      {h}
                      <button
                        onClick={() => setHobbies((p) => p.filter((x) => x !== h))}
                        type="button"
                        className="text-white/80 hover:text-white cursor-pointer"
                      >
                        <svg width="10" height="10" viewBox="0 0 10 10" fill="none">
                          <path d="M1 1l8 8M9 1l-8 8" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
                        </svg>
                      </button>
                    </motion.span>
                  ))}
                </motion.div>
              )}
            </AnimatePresence>
          </section>

          <section>
            <div className="flex items-center gap-2 mb-3">
              <Heart size={14} className="text-white/70" />
              <span className="text-[11px] text-white font-bold tracking-wide uppercase">
                Was mir gefällt
              </span>
            </div>

            <div className="flex flex-wrap gap-2 mb-3">
              {INTEREST_SUGGESTIONS.map((l) => (
                <motion.button
                  key={l}
                  whileTap={{ scale: 0.92 }}
                  onClick={() =>
                    likes.includes(l)
                      ? setLikes((p) => p.filter((x) => x !== l))
                      : setLikes((p) => [...p, l])
                  }
                  type="button"
                  className={`px-3 py-1.5 rounded-full text-[11px] font-bold transition-colors ${
                    likes.includes(l)
                      ? "bg-white text-[#EA580C]"
                      : "bg-white/20 text-white hover:bg-white/30"
                  }`}
                >
                  {l}
                </motion.button>
              ))}
            </div>

            <div className="flex gap-2">
              <div className="flex-1 bg-white rounded-full px-4 py-3 shadow-inner flex items-center gap-2">
                <Smile size={16} className="text-[#EA580C] flex-shrink-0" />
                <input
                  type="text"
                  value={newLike}
                  onChange={(e) => setNewLike(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === "Enter") { e.preventDefault(); addLike(newLike); }
                  }}
                  placeholder="Eigene Interesse…"
                  className="bg-transparent text-[13px] text-[#2D1A05] outline-none font-semibold placeholder-gray-300 w-full"
                />
              </div>
              <button
                onClick={() => addLike(newLike)}
                type="button"
                className="bg-white rounded-full w-11 h-11 flex items-center justify-center shadow-inner flex-shrink-0 hover:scale-105 transition-transform cursor-pointer"
              >
                <Smile size={16} className="text-[#EA580C]" />
              </button>
            </div>

            <AnimatePresence mode="popLayout">
              {likes.length > 0 && (
                <motion.div className="flex flex-wrap gap-2 mt-3" layout>
                  {likes.map((l, i) => (
                    <motion.span
                      key={l}
                      initial={{ scale: 0, opacity: 0 }}
                      animate={{ scale: 1, opacity: 1 }}
                      exit={{ scale: 0, opacity: 0 }}
                      layout
                      style={{ backgroundColor: BUBBLE_COLORS[(i + 4) % BUBBLE_COLORS.length] }}
                      className="inline-flex items-center gap-1.5 text-white text-[12px] font-bold px-3.5 py-1.5 rounded-full shadow-sm"
                    >
                      <Heart size={10} className="text-white/80" />
                      {l}
                      <button
                        onClick={() => setLikes((p) => p.filter((x) => x !== l))}
                        type="button"
                        className="text-white/80 hover:text-white cursor-pointer"
                      >
                        <svg width="10" height="10" viewBox="0 0 10 10" fill="none">
                          <path d="M1 1l8 8M9 1l-8 8" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
                        </svg>
                      </button>
                    </motion.span>
                  ))}
                </motion.div>
              )}
            </AnimatePresence>
          </section>
        </div>

        <div className="w-full px-8 mt-1">
          <button
            onClick={handleSave}
            disabled={saving || !token || loading}
            className="w-full py-4 bg-[#2A1405] hover:bg-[#1C0D03] rounded-full text-white font-bold text-[15px] shadow-md active:scale-[0.98] transition-all disabled:opacity-50 cursor-pointer"
          >
            {saving ? (
              <span className="flex items-center justify-center gap-2">
                <Loader2 size={16} className="animate-spin" />
                Wird gespeichert…
              </span>
            ) : (
              "Profil speichern"
            )}
          </button>
        </div>
      </motion.div>
    </div>
  );
}
