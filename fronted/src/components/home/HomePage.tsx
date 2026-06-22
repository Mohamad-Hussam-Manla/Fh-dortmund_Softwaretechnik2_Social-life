import { motion } from "framer-motion";
import { Heart, Tag, User, LogOut } from "lucide-react";
import UniversitySocialLifeLogo from "../ui/UniversitySocialLifeLogo";
import { useAuthStore } from "../../stores/authStore";

interface Props {
  onOpenProfile: () => void;
}

export function HomePage({ onOpenProfile }: Props) {
  const { user, logout } = useAuthStore();

  return (
    <div className="min-h-screen w-full bg-[#FFF9F5] flex flex-col items-center relative overflow-hidden">
      {/* --- Top gradient arc --- */}
      <div className="absolute top-0 inset-x-0 h-[320px] bg-gradient-to-br from-[#F97316] to-[#FDBA74] rounded-b-[50%] -z-0" />

      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4 }}
        className="relative z-10 w-full max-w-xl px-6 pt-12 pb-8 flex flex-col items-center"
      >
        {/* Logo */}
        <div className="w-60 mb-4">
          <UniversitySocialLifeLogo maxWidth="300px" />
        </div>

        {/* Welcome card */}
        <motion.div
          whileHover={{ scale: 1.02 }}
          className="w-full bg-white rounded-[32px] shadow-xl p-6 flex flex-col items-center gap-4 mt-2"
        >
          <p className="text-[#2A1405] text-[20px] font-extrabold">
            Willkommen, {user?.displayName ?? "Gast"}!
          </p>

          {/* Quick stats */}
          <div className="w-full grid grid-cols-3 gap-3 mt-1">
            {/* Hobbies count */}
            <div className="bg-orange-50 rounded-2xl p-4 flex flex-col items-center gap-1.5">
              <Tag size={22} className="text-[#EA580C]" />
              <span className="text-[22px] font-extrabold text-[#2A1405]">
                {user?.hobbies.length ?? 0}
              </span>
              <span className="text-[10px] text-gray-400 font-semibold uppercase tracking-wide">
                Hobbies
              </span>
            </div>

            {/* Likes count */}
            <div className="bg-orange-50 rounded-2xl p-4 flex flex-col items-center gap-1.5">
              <Heart size={22} className="text-[#EA580C]" />
              <span className="text-[22px] font-extrabold text-[#2A1405]">
                {user?.likes.length ?? 0}
              </span>
              <span className="text-[10px] text-gray-400 font-semibold uppercase tracking-wide">
                Interessen
              </span>
            </div>

            {/* Profile (button) */}
            <motion.button
              whileTap={{ scale: 0.95 }}
              onClick={onOpenProfile}
              className="bg-[#EA580C] rounded-2xl p-4 flex flex-col items-center gap-1.5 cursor-pointer"
            >
              <User size={22} className="text-white" />
              <span className="text-[10px] text-white/90 font-semibold uppercase tracking-wide">
                Profil
              </span>
            </motion.button>
          </div>
        </motion.div>

        {/* My profile summary */}
        {user && (user.hobbies.length > 0 || user.likes.length > 0) && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.3 }}
            className="w-full mt-6 space-y-4"
          >
            {user.hobbies.length > 0 && (
              <div className="bg-white rounded-[28px] shadow-lg p-5">
                <h3 className="text-[13px] font-extrabold text-[#2A1405] uppercase tracking-wide mb-2 flex items-center gap-2">
                  <Tag size={16} className="text-[#EA580C]" /> Meine Hobbies
                </h3>
                <div className="flex flex-wrap gap-2">
                  {user.hobbies.map((h) => (
                    <span key={h} className="bg-orange-100 text-[#EA580C] text-[12px] font-bold px-4 py-1.5 rounded-full">
                      {h}
                    </span>
                  ))}
                </div>
              </div>
            )}

            {user.likes.length > 0 && (
              <div className="bg-white rounded-[28px] shadow-lg p-5">
                <h3 className="text-[13px] font-extrabold text-[#2A1405] uppercase tracking-wide mb-2 flex items-center gap-2">
                  <Heart size={16} className="text-[#EA580C]" /> Was mir gefallt
                </h3>
                <div className="flex flex-wrap gap-2">
                  {user.likes.map((l) => (
                    <span key={l} className="bg-orange-100 text-[#EA580C] text-[12px] font-bold px-4 py-1.5 rounded-full">
                      {l}
                    </span>
                  ))}
                </div>
              </div>
            )}
          </motion.div>
        )}

        {/* Logout */}
        <div className="mt-auto pt-6">
          <button
            onClick={logout}
            className="flex items-center gap-2 text-[14px] font-semibold text-gray-400 hover:text-[#EA580C] transition-colors cursor-pointer"
          >
            <LogOut size={18} />
            Abmelden
          </button>
        </div>
      </motion.div>
    </div>
  );
}
