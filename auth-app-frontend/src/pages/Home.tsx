import { Link } from "react-router"; // Use 'react-router-dom' if on older versions

export default function Home() {
  return (
    <div className="bg-slate-950 text-slate-100 min-h-screen flex flex-col selection:bg-emerald-500 selection:text-slate-950">
      
      {/* HERO SECTION */}
      <section className="flex-1 max-w-6xl mx-auto px-6 pt-20 pb-16 flex flex-col items-center justify-center text-center space-y-8">
        
        {/* Micro-badge Announcement */}
        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs font-mono tracking-wider uppercase">
          <span className="flex h-2 w-2 rounded-full bg-emerald-400 animate-pulse" />
          v1.0.0 Production Architecture Active
        </div>

        {/* Main Value Proposition */}
        <h1 className="text-4xl md:text-6xl font-black tracking-tight max-w-3xl leading-tight">
          Enterprise Authentication, <br />
          <span className="bg-gradient-to-r from-emerald-400 via-teal-400 to-cyan-400 bg-clip-text text-transparent">
            Engineered For Scale.
          </span>
        </h1>

        <p className="text-slate-400 text-base md:text-xl max-w-2xl font-light leading-relaxed">
          A production-grade, self-contained authentication ecosystem. Built with a high-performance React front-end, global state persistence, and a cryptographically hardened Spring Security JWT engine.
        </p>

        {/* Call to Action Controls */}
        <div className="flex flex-col sm:flex-row gap-4 justify-center items-center pt-4 w-full sm:w-auto">
          <Link
            to="/signup"
            className="w-full sm:w-auto text-center bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-bold px-8 py-3.5 rounded-lg shadow-lg shadow-emerald-500/20 transition default-focus"
          >
            Create Secure Account
          </Link>
          <Link
            to="/login"
            className="w-full sm:w-auto text-center bg-slate-900 hover:bg-slate-800 border border-slate-800 text-slate-200 font-medium px-8 py-3.5 rounded-lg transition default-focus"
          >
            Sign In to Console
          </Link>
        </div>

      </section>

      {/* CORE SPECIFICATIONS METRIC GRID */}
      <section className="border-t border-slate-900 bg-slate-900/40 py-16">
        <div className="max-w-6xl mx-auto px-6">
          <div className="grid gap-8 sm:grid-cols-2 lg:grid-cols-3">
            
            {/* Feature 1 */}
            <div className="bg-slate-900/60 border border-slate-800/60 p-6 rounded-xl space-y-3">
              <div className="text-emerald-400 text-xl font-mono">01 / SECURITY</div>
              <h3 className="text-lg font-bold text-slate-200">Stateful Context Guarding</h3>
              <p className="text-sm text-slate-400 leading-relaxed">
                Zustand framework acts as the centralized authority, handling secure hydration cycles and protecting layout boundaries from memory-jacking vectors.
              </p>
            </div>

            {/* Feature 2 */}
            <div className="bg-slate-900/60 border border-slate-800/60 p-6 rounded-xl space-y-3">
              <div className="text-teal-400 text-xl font-mono">02 / PIPELINE</div>
              <h3 className="text-lg font-bold text-slate-200">Asynchronous Interceptors</h3>
              <p className="text-sm text-slate-400 leading-relaxed">
                Axios pipeline execution layers dynamically capture outbound data vectors to inject authorization claims, while managing systemic error responses natively.
              </p>
            </div>

            {/* Feature 3 */}
            <div className="bg-slate-900/60 border border-slate-800/60 p-6 rounded-xl space-y-3 sm:col-span-2 lg:col-span-1">
              <div className="text-cyan-400 text-xl font-mono">03 / ENGINE</div>
              <h3 className="text-lg font-bold text-slate-200">Stateless Spring Filter-Chain</h3>
              <p className="text-sm text-slate-400 leading-relaxed">
                Backed by Java 17+ enterprise logic. Validates signatures using cryptographically signed JSON Web Tokens over secure cross-origin configurations.
              </p>
            </div>

          </div>
        </div>
      </section>

    </div>
  );
}