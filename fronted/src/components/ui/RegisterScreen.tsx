import { useState } from "react";
import { motion } from "framer-motion";
import { User, Mail, Lock, Eye, EyeOff, ChevronLeft, Check } from "lucide-react";
import UniversitySocialLifeLogo from "./UniversitySocialLifeLogo";
import type { UserProfile } from "../../types";

interface ScreenProps {
  onSuccess: (token: string, user: UserProfile) => void;
  onSwitch: () => void;
}

export const RegisterScreen = ({ onSuccess, onSwitch }: ScreenProps) => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [name, setName] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const accountType = email.endsWith("@fh-dortmund.de") 
    ? "professor" 
    : email.endsWith("@stud.fh-dortmund.de") 
    ? "student" 
    : null;

  const handleRegister = async () => {
    setError("");
    if (!email || !password || !name) return setError("Bitte füllen Sie alle Felder aus");
    if (!accountType) return setError("Nur gültige Universität-E-Mail-Adressen sind erlaubt");
    if (password !== confirmPassword) return setError("Passwörter stimmen nicht überein");

    setIsLoading(true);
    try {
      // التعديل الجوهري: توجيه الطلب إلى السيرفر المحلي والـ Docker عبر الـ Proxy الصحيح
      const response = await fetch("/api/auth/register", {
        method: "POST",
        headers: { 
          "Content-Type": "application/json",
          "X-Requested-With": "XMLHttpRequest"
        },
        body: JSON.stringify({ 
          displayName: name, // تأكدت أن الباك إند يتوقع displayName حسب كود صديقك
          universityEmail: email, 
          password: password 
        }),
      });

      const body = await response.json().catch(() => ({}));

      if (response.ok) {
        onSwitch();
        return;
      }
      setError(body.message || "Registrierung fehlgeschlagen");
    } catch (err) {
      setError("Serverfehler");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen w-full bg-gradient-to-br from-[#F97316] to-[#FDBA74] justify-center items-center px-4 py-8 relative">
      
      {/* زر العودة العلوي الذكي خارج الصندوق */}
      <div className="absolute top-6 left-6">
        <button onClick={onSwitch} className="flex items-center gap-1.5 text-white font-semibold cursor-pointer transition-opacity hover:opacity-80">
          <ChevronLeft size={20} />
          <span className="text-[14px]">Zurück</span>
        </button>
      </div>

      <motion.div
        initial={{ scale: 0.95, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        className="w-full max-w-[420px] bg-[#EA580C] rounded-[40px] shadow-[0_25px_50px_-12px_rgba(0,0,0,0.4)] overflow-hidden pb-10 flex flex-col items-center"
      >
        {/* القوس العلوي المتطابق */}
        <div className="w-[140%] bg-white rounded-b-[100%] flex justify-center items-center pt-10 pb-5 shadow-sm mb-5 -mt-2">
          <div className="transform scale-[0.6] origin-center">
            <UniversitySocialLifeLogo maxWidth="400px" />
          </div>
        </div>

        <div className="text-center px-6 mb-6">
          <h1 className="text-[30px] font-extrabold text-white leading-tight mb-1">
            Registrierung
          </h1>
          <p className="text-white/80 text-[13px] font-medium">
            Erstellen Sie Ihr Konto
          </p>
        </div>

        <div className="w-full px-8 space-y-3.5">
          {/* حقل الاسم */}
          <div className="bg-white rounded-full px-5 py-3 shadow-inner flex items-center gap-3">
            <User size={18} className="text-[#EA580C] flex-shrink-0" />
            <div className="flex-1 flex flex-col">
              <span className="text-[10px] text-gray-400 font-bold leading-none mb-0.5">Ihr Name</span>
              <input type="text" value={name} onChange={(e) => setName(e.target.value)} placeholder="Max Mustermann" className="bg-transparent text-[14px] text-[#2D1A05] outline-none font-semibold placeholder-gray-300 w-full" />
            </div>
          </div>

          {/* حقل الإيميل */}
          <div className="bg-white rounded-full px-5 py-3 shadow-inner flex items-center gap-3">
            <Mail size={18} className="text-[#EA580C] flex-shrink-0" />
            <div className="flex-1 flex flex-col">
              <span className="text-[10px] text-gray-400 font-bold leading-none mb-0.5">E-Mail Adresse</span>
              <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="name@stud.fh-dortmund.de" className="bg-transparent text-[14px] text-[#2D1A05] outline-none font-semibold placeholder-gray-300 w-full" />
            </div>
          </div>

          {/* إشعار نوع الحساب التلقائي */}
          {accountType && (
            <div className="flex items-center gap-2 bg-[#2A1405] rounded-full px-4 py-2.5 shadow-md justify-center">
              <Check size={16} className="text-orange-400" />
              <p className="text-[11px] text-white font-extrabold uppercase tracking-wider">
                {accountType === "professor" ? "Professor" : "Student"}-Konto erkannt
              </p>
            </div>
          )}

          {/* حقل كلمة المرور */}
          <div className="bg-white rounded-full px-5 py-3 shadow-inner flex items-center gap-3">
            <Lock size={18} className="text-[#EA580C] flex-shrink-0" />
            <div className="flex-1 flex flex-col">
              <span className="text-[10px] text-gray-400 font-bold leading-none mb-0.5">Passwort</span>
              <input type={showPassword ? "text" : "password"} value={password} onChange={(e) => setPassword(e.target.value)} placeholder="••••••••" className="bg-transparent text-[14px] text-[#2D1A05] outline-none font-semibold placeholder-gray-300 w-full" />
            </div>
            <button onClick={() => setShowPassword(!showPassword)} type="button" className="text-gray-400 hover:text-[#EA580C] cursor-pointer">
              {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
            </button>
          </div>

          {/* حقل تأكيد كلمة المرور */}
          <div className="bg-white rounded-full px-5 py-3 shadow-inner flex items-center gap-3">
            <Lock size={18} className="text-[#EA580C] flex-shrink-0" />
            <div className="flex-1 flex flex-col">
              <span className="text-[10px] text-gray-400 font-bold leading-none mb-0.5">Bestätigen</span>
              <input type={showPassword ? "text" : "password"} value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} placeholder="••••••••" className="bg-transparent text-[14px] text-[#2D1A05] outline-none font-semibold placeholder-gray-300 w-full" />
            </div>
          </div>

          {error && (
            <div className="bg-rose-600 rounded-xl px-4 py-2 text-[11px] text-white font-semibold">
              {error}
            </div>
          )}

          {/* زر التسجيل البني الغامق */}
          <button onClick={handleRegister} disabled={isLoading} className="w-full py-4 bg-[#2A1405] hover:bg-[#1C0D03] rounded-full text-white font-bold text-[15px] shadow-md transition-all active:scale-[0.98] disabled:opacity-50 cursor-pointer mt-2">
            {isLoading ? "Laden..." : "Konto erstellen"}
          </button>
        </div>
      </motion.div>
    </div>
  );
};