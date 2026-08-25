export default function Footer() {
  return (
    <footer className="bg-bg-dark border-t border-white/10">
      <div className="max-w-[1200px] mx-auto px-6 py-14 flex flex-col md:flex-row items-center justify-between gap-6">
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-lg bg-accent flex items-center justify-center text-white font-bold text-sm">
            M
          </div>
          <span className="font-semibold text-white tracking-tight">MiniCommerce</span>
        </div>
        <p className="text-text-muted-dark text-sm">
          © {new Date().getFullYear()} MiniCommerce. All rights reserved.
        </p>
        <div className="flex gap-3">
          <div className="w-9 h-9 rounded-full border border-white/10 flex items-center justify-center text-white/40 hover:text-accent hover:border-accent/40 transition-colors cursor-pointer">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M18 2h-3a5 5 0 0 0-5 5v3H7v4h3v8h4v-8h3l1-4h-4V7a1 1 0 0 1 1-1h3z"></path></svg>
          </div>
          <div className="w-9 h-9 rounded-full border border-white/10 flex items-center justify-center text-white/40 hover:text-accent hover:border-accent/40 transition-colors cursor-pointer">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="2" y="2" width="20" height="20" rx="5" ry="5"></rect><path d="M16 11.37A4 4 0 1 1 12.63 8 4 4 0 0 1 16 11.37z"></path><line x1="17.5" y1="6.5" x2="17.51" y2="6.5"></line></svg>
          </div>
        </div>
      </div>
    </footer>
  );
}
