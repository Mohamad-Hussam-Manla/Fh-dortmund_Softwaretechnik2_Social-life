import { useState } from "react";
import { motion } from "framer-motion";
import { Lock, Mail, Eye, EyeOff, AlertCircle } from "lucide-react";
import UniversitySocialLifeLogo from "./UniversitySocialLifeLogo";

interface ScreenProps {
  onSuccess: () => void;
  onSwitch: () => void;
}

export const LoginScreen = ({ onSuccess, onSwitch }: ScreenProps) => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const handleLogin = async () => {
    setError("");
    if (!email || !password)
      return setError("Bitte füllen Sie alle Felder aus");

    setIsLoading(true);
    try {
      // تعديل المسار هنا بإضافة v1 ليطابق الباك إند تماماً لزميلك
      const response = await fetch("/api/auth/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          // هذا السطر يمنع المتصفح نهائياً من إظهار النافذة المزعجة في حال عدم تطابق البيانات
          "X-Requested-With": "XMLHttpRequest",
        },
        body: JSON.stringify({
          universityEmail: email,
          password: password,
        }),
      });

      const data = await response.json().catch(() => ({}));

      if (response.ok) {
        if (data && data.token) {
          localStorage.setItem("token", data.token); // حفظ التوكن بنجاح
        }
        onSuccess(); // تشغيل دالة النجاح والانتقال للرئيسية
      } else {
        // إذا كانت البيانات خاطئة ستظهر هنا كرسالة نصية عادية بدون النوافذ المنبثقة
        setError(
          data.message || "Anmeldung fehlgeschlagen. Bitte Daten prüfen.",
        );
      }
    } catch (err) {
      setError("Serververbindung fehlgeschlagen");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen w-full bg-gradient-to-br from-[#F97316] to-[#FDBA74] justify-center items-center px-4 py-8">
      <motion.div
        initial={{ scale: 0.95, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        className="w-full max-w-[420px] bg-[#EA580C] rounded-[40px] shadow-[0_25px_50px_-12px_rgba(0,0,0,0.4)] overflow-hidden pb-10 relative flex flex-col items-center"
      >
        {/* الشعار العلوي */}
        <div className="w-[140%] bg-white rounded-b-[100%] flex justify-center items-center pt-10 pb-5 shadow-sm mb-5 -mt-2">
          <div className="transform scale-[0.6] origin-center">
            <UniversitySocialLifeLogo maxWidth="400px" />
          </div>
        </div>

        {/* العناوين */}
        <div className="text-center px-6 mb-6">
          <h1 className="text-[32px] font-extrabold text-white leading-tight mb-1">
            Anmeldung
          </h1>
          <p className="text-white/80 text-[13px] font-medium">
            Willkommen zurück
          </p>
        </div>

        {/* الحقول ومحتوى المدخلات */}
        <div className="w-full px-8 space-y-4">
          {/* حقل الإيميل */}
          <div className="bg-white rounded-full px-5 py-3.5 shadow-inner flex items-center gap-3">
            <Mail size={18} className="text-[#EA580C] flex-shrink-0" />
            <div className="flex-1 flex flex-col">
              <span className="text-[10px] text-gray-400 font-bold leading-none mb-0.5">
                E-Mail Adresse
              </span>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="name@stud.fh-dortmund.de"
                className="bg-transparent text-[14px] text-[#2D1A05] outline-none font-semibold placeholder-gray-300 w-full"
              />
            </div>
          </div>

          {/* حقل الباسورد */}
          <div className="bg-white rounded-full px-5 py-3.5 shadow-inner flex items-center gap-3">
            <Lock size={18} className="text-[#EA580C] flex-shrink-0" />
            <div className="flex-1 flex flex-col">
              <span className="text-[10px] text-gray-400 font-bold leading-none mb-0.5">
                Passwort
              </span>
              <input
                type={showPassword ? "text" : "password"}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className="bg-transparent text-[14px] text-[#2D1A05] outline-none font-semibold placeholder-gray-300 w-full"
              />
            </div>
            <button
              onClick={() => setShowPassword(!showPassword)}
              type="button"
              className="text-gray-400 hover:text-[#EA580C] cursor-pointer"
            >
              {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
            </button>
          </div>

          {error && (
            <div className="flex items-center gap-2 bg-rose-600 rounded-xl px-4 py-2">
              <AlertCircle size={14} className="text-white" />
              <p className="text-[11px] text-white font-semibold">{error}</p>
            </div>
          )}

          {/* زر تسجيل الدخول */}
          <button
            onClick={handleLogin}
            disabled={isLoading}
            className="w-full py-4 bg-[#2A1405] hover:bg-[#1C0D03] rounded-full text-white font-bold text-[15px] shadow-md active:scale-[0.98] transition-all mt-2 disabled:opacity-50 cursor-pointer"
          >
            {isLoading ? "Wird geladen..." : "Anmelden"}
          </button>

          <div className="text-center pt-2 flex flex-col items-center gap-0.5">
            <span className="text-white/80 text-[13px] font-medium">
              Noch kein Konto?
            </span>
            <button
              onClick={onSwitch}
              type="button"
              className="text-white text-[14px] font-bold underline hover:text-white/90 cursor-pointer"
            >
              Konto erstellen
            </button>
          </div>
        </div>
      </motion.div>
    </div>
  );
};
