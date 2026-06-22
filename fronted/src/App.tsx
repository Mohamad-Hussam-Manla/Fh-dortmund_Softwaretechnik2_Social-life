import { useState, useEffect, useCallback } from "react"; // 1. أضفنا useEffect
import { AnimatePresence, motion } from "framer-motion";
import { LoginScreen } from "./components/ui/LoginScreen";
import { RegisterScreen } from "./components/ui/RegisterScreen";
import { ProfileScreen } from "./components/ui/ProfileScreen";
import { HomePage } from "./components/home/HomePage";
import { Check } from "lucide-react";
import { useAuthStore } from "./stores/authStore";
import type { UserProfile } from "./types";

type Screen = "login" | "register" | "home" | "profile";

export default function App() {
  const login = useAuthStore((s) => s.login);
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  
  // جعل الشاشة الافتراضية تتحدد بناءً على حالة Zustand الحقيقية لمنع التناقض
  const [screen, setScreen] = useState<Screen>("login");
  const [showSuccess, setShowSuccess] = useState(false);
  const [isInitialized, setIsInitialized] = useState(false); // لمنع الوميض والطلبات العشوائية قبل فحص التوكن

  // 2. مزامنة الـ Store مع الـ localStorage فور تشغيل التطبيق وقبل إرسال أي طلبات
  useEffect(() => {
    const token = localStorage.getItem("token");
    const userProfileStr = localStorage.getItem("userProfile");

    if (token && userProfileStr) {
      try {
        const user = JSON.parse(userProfileStr);
        login(token, user); // تحديث الـ Zustand Store ليصبح isAuthenticated = true
        setScreen("home");
      } catch (e) {
        localStorage.clear();
      }
    }
    setIsInitialized(true);
  }, [login]);

  const handleSuccessfulAuth = useCallback(
    (token: string, user: UserProfile) => {
      login(token, user);
      setShowSuccess(true);
      setTimeout(() => {
        setShowSuccess(false);
        setScreen("profile");
      }, 1500);
    },
    [login],
  );

  // انتظر حتى تنتهي عملية الفحص المبدئية للتوكن
  if (!isInitialized) {
    return <div className="min-h-screen bg-[#FFF9F5] flex items-center justify-center">Loading...</div>;
  }

  return (
    <div className="min-h-screen w-full bg-[#FFF9F5] flex items-center justify-center relative overflow-hidden">
      <AnimatePresence mode="wait">
        {/* --- success toast --- */}
        {showSuccess && (
          <motion.div
            key="success"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-emerald-500 flex flex-col items-center justify-center text-white p-6 text-center z-50"
          >
            <div className="w-20 h-20 bg-white/20 rounded-full flex items-center justify-center mb-4">
              <Check size={40} strokeWidth={3} />
            </div>
            <h2 className="text-2xl font-bold">Erfolgreich!</h2>
          </motion.div>
        )}

        {/* --- الفرز الصحيح بناءً على Zustand الصادق --- */}
        {isAuthenticated && !showSuccess ? (
          screen === "profile" ? (
            <ProfileScreen key="profile" onBack={() => setScreen("home")} />
          ) : (
            <HomePage key="home" onOpenProfile={() => setScreen("profile")} />
          )
        ) : !showSuccess ? (
          /* في حال عدم تسجيل الدخول، نضمن أن القيمة الممررة إما login أو register فقط */
          <AuthScreen 
            key={screen} 
            screen={screen === "register" ? "register" : "login"} 
            onSuccess={handleSuccessfulAuth} 
            onSwitch={() => setScreen(screen === "login" ? "register" : "login")} 
          />
        ) : null}
      </AnimatePresence>
    </div>
  );
}

/* ---- المكون الفرعي لم يتغير ولكن تمت حمايته من القيم العشوائية ---- */
function AuthScreen({
  screen,
  onSuccess,
  onSwitch,
}: {
  screen: "login" | "register";
  onSuccess: (token: string, user: UserProfile) => void;
  onSwitch: () => void;
}) {
  return (
    <motion.div
      initial={{ x: screen === "login" ? -50 : 50, opacity: 0 }}
      animate={{ x: 0, opacity: 1 }}
      exit={{ x: screen === "login" ? 50 : -50, opacity: 0 }}
      transition={{ duration: 0.3 }}
      className="w-full min-h-screen"
    >
      {screen === "login" ? (
        <LoginScreen onSuccess={onSuccess} onSwitch={onSwitch} />
      ) : (
        <RegisterScreen onSuccess={onSuccess} onSwitch={onSwitch} />
      )}
    </motion.div>
  );
}