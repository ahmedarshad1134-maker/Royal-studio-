import React from 'react';
import { useApp } from '../context/AppContext';
import { CheckCircle2, ChevronRight, Sparkles, Calendar } from 'lucide-react';

export const ServicesScreen: React.FC = () => {
  const { services, navigateTo } = useApp();

  return (
    <div className="min-h-screen bg-[#0E0E10] text-[#EBEBEB] py-16 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto space-y-16">
        {/* Header */}
        <div className="text-center max-w-3xl mx-auto space-y-4">
          <div className="inline-flex items-center space-x-2 px-3 py-1 rounded-full bg-[#D4AF37]/15 border border-[#D4AF37]/30 text-[#D4AF37] text-xs font-semibold uppercase tracking-widest">
            <Sparkles className="w-3.5 h-3.5" />
            <span>Excellence In Motion & Stills</span>
          </div>
          <h1 className="font-serif text-4xl sm:text-6xl font-bold text-white tracking-tight">
            Our Studio Services
          </h1>
          <p className="text-sm sm:text-base text-[#EBEBEB]/70 font-light leading-relaxed">
            From comprehensive all-day wedding films to bespoke heirloom albums, explore our full spectrum of fine-art visual services.
          </p>
        </div>

        {/* Services Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {services
            .filter((s) => s.enabled)
            .map((svc) => (
              <div
                key={svc.id}
                className="rounded-2xl bg-[#141416] border border-white/5 hover:border-[#D4AF37]/40 overflow-hidden shadow-xl transition-all duration-300 flex flex-col justify-between group"
              >
                <div>
                  <div className="relative h-64 overflow-hidden bg-black">
                    <img
                      src={svc.imageUrl}
                      alt={svc.name}
                      className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
                    />
                    <div className="absolute inset-0 bg-gradient-to-t from-[#141416] via-transparent to-transparent" />
                    <div className="absolute top-4 right-4 px-3.5 py-1 rounded-full bg-black/80 backdrop-blur-md border border-[#D4AF37]/30 text-[#D4AF37] text-xs font-bold">
                      Starting {svc.startingPrice}
                    </div>
                  </div>

                  <div className="p-6 space-y-4">
                    <h3 className="font-serif text-2xl font-bold text-white group-hover:text-[#D4AF37] transition-colors">
                      {svc.name}
                    </h3>
                    <p className="text-xs sm:text-sm text-[#EBEBEB]/70 leading-relaxed font-light">
                      {svc.shortDescription}
                    </p>

                    <div className="space-y-2 pt-2 border-t border-white/5">
                      <p className="text-[11px] font-bold text-[#D4AF37] uppercase tracking-wider">
                        Included Features
                      </p>
                      {svc.features.map((feat, fIdx) => (
                        <div key={fIdx} className="flex items-center space-x-2 text-xs text-[#EBEBEB]/80">
                          <CheckCircle2 className="w-3.5 h-3.5 text-[#D4AF37] shrink-0" />
                          <span>{feat}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>

                <div className="p-6 pt-0 flex items-center space-x-3">
                  <button
                    onClick={() => navigateTo('service_detail', { serviceId: svc.id })}
                    className="flex-1 py-2.5 rounded-xl bg-white/5 hover:bg-white/10 text-white font-medium text-xs border border-white/10 hover:border-[#D4AF37]/30 transition"
                  >
                    View Details
                  </button>
                  <button
                    onClick={() => navigateTo('booking', { serviceId: svc.id })}
                    className="flex-1 py-2.5 rounded-xl bg-[#D4AF37] hover:bg-[#AA8C2C] text-black font-bold text-xs uppercase tracking-wider transition"
                  >
                    Book Service
                  </button>
                </div>
              </div>
            ))}
        </div>
      </div>
    </div>
  );
};
