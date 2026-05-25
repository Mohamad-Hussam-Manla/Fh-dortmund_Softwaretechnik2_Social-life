/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}", // 👈 هذا يضمن قراءة الـ tsx
    ],
    theme: {
        extend: {},
    },
    plugins: [],
}