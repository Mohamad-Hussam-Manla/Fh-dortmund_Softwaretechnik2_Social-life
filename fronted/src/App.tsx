import { useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { LoginScreen } from "./components/ui/LoginScreen";
import { RegisterScreen } from "./components/ui/RegisterScreen";
import { Check } from "lucide-react";

export type AuthScreen = "login" | "register";

export default function App() {
  const [authScreen, setAuthScreen] = useState<AuthScreen>("login");
  const [isSuccess, setIsSuccess] = useState(false);

  const handleAuthAction = () => {
    setIsSuccess(true);
    setTimeout(() => setIsSuccess(false), 3000);
  };

  return (
    <div className="min-h-screen w-full bg-[#FFF9F5] flex items-center justify-center relative overflow-hidden">
      <AnimatePresence mode="wait">
        {isSuccess ? (
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
        ) : (
          <motion.div 
            key={authScreen} 
            initial={{ x: authScreen === "login" ? -50 : 50, opacity: 0 }} 
            animate={{ x: 0, opacity: 1 }} 
            exit={{ x: authScreen === "login" ? 50 : -50, opacity: 0 }} 
            transition={{ duration: 0.3 }} 
            className="w-full min-h-screen"
          >
            {authScreen === "login" ? (
              <LoginScreen onSuccess={handleAuthAction} onSwitch={() => setAuthScreen("register")} />
            ) : (
              <RegisterScreen onSuccess={handleAuthAction} onSwitch={() => setAuthScreen("login")} />
            )}
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}