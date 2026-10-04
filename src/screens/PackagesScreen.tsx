import React from 'react';
import { useApp } from '../context/AppContext';
import { CheckCircle2, Sparkles, Star, ArrowRight, ShieldCheck, Clock, Users } from 'lucide-react';

export const PackagesScreen: React.FC = () => {
  const { packages, navigateTo } = useApp();

  return (
    <div className="min-h-screen bg-[#0E0E10] text-[#EBEBEB] py-16 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto space-y-16">
        {/* Header */}
        <div className="text-center max-w-3xl mx-auto space-y-4">
          <div className="inline-flex items-center space-x-2 px-3 py-1 rounded-full bg-[#D4AF37]/15 border border-[#D4AF37]/30 text-[#D4AF37] text-xs font-semibold uppercase tracking-widest">
            <Sparkles className="w-3.5 h-3.5" />
            <span>Investment & Collections</span>
          </div>
          <h1 className="font-serif text-4xl sm:text-6xl font-bold text-white tracking-tight">
            Curated Signature Packages
          </h1>
          <p className="text-sm sm:text-base text-[#EBEBEB]/70 font-light leading-relaxed">
            Transparent collections engineered to ensure comprehensive coverage of your celebration, from intimate elopements to royal celebrations.
          </p>
        </div>

        {/* Packages Cards Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 items-stretch">
          {packages
            .filter((p) => p.enabled)
            .map((pkg) => {
              const isRecommended = pkg.isRecommended;
              return (
                <div
                  key={pkg.id}
                  className={`rounded-3xl p-6 sm:p-8 flex flex-col justify-between transition-all duration-300 relative ${
                    isRecommended
                      ? 'bg-gradient-to-b from-[#1F1F24] via-[#17171A] to-[#121214] border-2 border-[#D4AF37] shadow-2xl shadow-[#D4AF37]/15 scale-100 lg:-translate-y-2'
                      : 'bg-[#141416] border border-white/5 hover:border-[#D4AF37]/40 shadow-xl'
                  }`}
                >
                  {isRecommended && (
                    <div className="absolute -top-3.5 left-1/2 -translate-x-1/2 px-4 py-1 rounded-full bg-[#D4AF37] text-black text-[11px] font-extrabold uppercase tracking-widest shadow-md">
                      Most Popular Choice
                    </div>
                  )}

                  <div className="space-y-6">
                    <div className="space-y-2">
                      <h3 className="font-serif text-2xl font-bold text-white">
                        {pkg.name}
                      </h3>
                      <p className="text-xs text-[#EBEBEB]/70 min-h-[36px] font-light leading-relaxed">
                        {pkg.description}
                      </p>
                    </div>

                    <div className="p-4 rounded-2xl bg-white/[0.03] border border-white/5">
                      <div className="flex items-baseline space-x-1">
                        <span className="text-sm font-semibold text-[#D4AF37]">{pkg.currency}</span>
                        <span className="font-serif text-4xl font-bold text-white tracking-tight">
                          {pkg.price}
                        </span>
                      </div>
                      <p className="text-[10px] text-[#EBEBEB]/50 mt-1 uppercase tracking-wider">
                        {pkg.price === 'Custom' ? 'Bespoke Quote' : 'Complete Coverage'}
                      </p>
                    </div>

                    {/* Specifications */}
                    <div className="space-y-2 text-xs text-[#EBEBEB]/80 pt-2 border-t border-white/5">
                      <div className="flex items-center space-x-2">
                        <Clock className="w-3.5 h-3.5 text-[#D4AF37] shrink-0" />
                        <span>Coverage: {pkg.photographyHours}</span>
                      </div>
                      <div className="flex items-center space-x-2">
                        <Users className="w-3.5 h-3.5 text-[#D4AF37] shrink-0" />
                        <span>
                          Crew: {pkg.numberOfPhotographers} Photo
                          {pkg.numberOfVideographers > 0 ? ` + ${pkg.numberOfVideographers} Cinema` : ''}
                        </span>
                      </div>
                    </div>

                    {/* Features checklist */}
                    <div className="space-y-2.5 pt-2 border-t border-white/5">
                      <p className="text-[10px] font-bold text-[#D4AF37] uppercase tracking-wider">
                        Included Deliverables
                      </p>
                      {pkg.includedServices.map((feat, fIdx) => (
                        <div key={fIdx} className="flex items-start space-x-2.5 text-xs text-[#EBEBEB]/80">
                          <CheckCircle2 className="w-4 h-4 text-[#D4AF37] shrink-0 mt-0.5" />
                          <span>{feat}</span>
                        </div>
                      ))}
                    </div>
                  </div>

                  <div className="pt-8 space-y-2">
                    <button
                      onClick={() => navigateTo('booking', { packageId: pkg.id })}
                      className={`w-full py-3 rounded-xl font-bold text-xs uppercase tracking-wider transition ${
                        isRecommended
                          ? 'bg-[#D4AF37] hover:bg-[#AA8C2C] text-black shadow-lg shadow-[#D4AF37]/20'
                          : 'bg-white/10 hover:bg-[#D4AF37] hover:text-black text-white'
                      }`}
                    >
                      Select & Inquire
                    </button>
                    <button
                      onClick={() => navigateTo('package_detail', { packageId: pkg.id })}
                      className="w-full py-2 text-center text-xs text-[#EBEBEB]/60 hover:text-[#D4AF37] transition"
                    >
                      View Detailed Specs →
                    </button>
                  </div>
                </div>
              );
            })}
        </div>
      </div>
    </div>
  );
};
