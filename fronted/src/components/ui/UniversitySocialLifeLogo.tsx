const UniversitySocialLifeLogo = ({ width = "100%", maxWidth = "500px" }) => {
  return (
    <div 
      className="flex flex-col items-center justify-center font-sans selection:bg-orange-200"
      style={{ width, maxWidth }}
    >
      {/* --- LOGO MARK (SVG) --- */}
      <svg 
        viewBox="0 0 500 500" 
        className="w-full h-auto mb-6 drop-shadow-sm"
        xmlns="http://www.w3.org/2000/svg"
      >
        {/* Orange Circular Background Fluid Shape */}
        <path 
          d="M 250,50 C 360,50 450,140 450,250 C 450,360 360,450 250,450 C 140,450 50,360 50,250 C 50,140 140,50 250,50 Z M 210,95 C 150,120 100,180 100,250 C 100,310 140,370 200,395 C 180,350 170,300 185,250 C 195,215 220,180 250,160 C 230,140 215,115 210,95 Z" 
          fill="#F26522" 
        />
        
        {/* Outer Swirling Accent */}
        <path 
          d="M 250,25 C 385,25 475,125 465,260 C 455,350 390,430 300,465 C 360,430 420,350 420,260 C 420,150 340,55 250,25 Z" 
          fill="#E3530D" 
        />

        {/* Abstract People / Figures (White & Shaded) */}
        {/* Left Figure */}
        <path 
          d="M 160,190 C 140,220 135,270 155,310 C 170,340 200,370 240,380 C 210,340 190,290 195,240 C 200,200 225,175 250,165 C 210,165 180,175 160,190 Z" 
          fill="#FFFFFF" 
        />
        <circle cx="165" cy="155" r="20" fill="#FFFFFF" />

        {/* Center Figure holding flame */}
        <path 
          d="M 250,250 C 220,270 215,310 230,340 C 245,370 280,385 310,360 C 280,350 260,310 265,280 C 270,255 295,245 310,245 C 280,230 260,235 250,250 Z" 
          fill="#F7F7F7" 
        />
        <circle cx="245" cy="215" r="18" fill="#FFFFFF" />
        
        {/* The Flame */}
        <path 
          d="M 305,190 C 300,205 310,215 315,225 C 300,225 290,215 292,200 C 295,185 305,175 305,175 C 305,175 315,185 305,190 Z" 
          fill="#E3530D" 
        />

        {/* Right Figure embracing the tower */}
        <path 
          d="M 360,265 C 380,290 410,330 380,390 C 350,420 290,425 260,395 C 310,390 350,350 340,300 C 335,280 320,265 305,260 C 330,250 350,255 360,265 Z" 
          fill="#FFFFFF" 
        />
        <circle cx="340" cy="235" r="18" fill="#FFFFFF" />

        {/* University Clock Tower / Steeple */}
        <g stroke="#E3530D" strokeWidth="6" strokeLinecap="round" strokeLinejoin="round" fill="none">
          {/* Roof Spire */}
          <path d="M 340,75 L 340,110" />
          {/* Triangular Roof */}
          <polygon points="315,150 340,110 365,150" fill="#FFFFFF" strokeWidth="5" />
          {/* Main Tower Body */}
          <rect x="319" y="150" width="42" height="70" fill="#FFFFFF" />
          {/* "U" Window Detail */}
          <path d="M 333,180 L 333,195 C 333,200 347,200 347,195 L 347,180" strokeWidth="5" />
        </g>
      </svg>

      {/* --- TYPOGRAPHY SECTION --- */}
      <div className="text-center tracking-wide">
        {/* Primary Brand Name */}
        <h1 className="text-4xl md:text-5xl font-black text-[#5C2D16] m-0 leading-none tracking-tight font-sans">
          UNIVERSITY
        </h1>
        
        {/* Secondary Brand Name */}
        <h2 className="text-3xl md:text-4xl font-semibold text-[#7A4326] mt-1 mb-3 tracking-wide font-sans">
          SOCIAL LIFE
        </h2>
        
        {/* Divider Line */}
        <div className="w-16 h-0.5 bg-orange-500 mx-auto mb-3 opacity-60" />

        {/* Tagline */}
        <p className="text-xs md:text-sm font-medium text-[#7A4326] tracking-widest uppercase flex items-center justify-center gap-2">
          <span>Connect</span>
          <span className="text-orange-500 text-base font-bold leading-none">•</span>
          <span>Engage</span>
          <span className="text-orange-500 text-base font-bold leading-none">•</span>
          <span>Community</span>
        </p>
      </div>
    </div>
  );
};

export default UniversitySocialLifeLogo;