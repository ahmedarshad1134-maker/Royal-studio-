import React from 'react';
import { useApp } from '../context/AppContext';
import { ArrowLeft, CheckCircle2, Calendar, ShieldCheck, Sparkles, Clock, Users } from 'lucide-react';

export const ServiceDetailScreen: React.FC = () => {
  const { screenParams, getServiceById, navigateTo, goBack } = useApp();
  const service = screenParams.serviceId ? getServiceById(screenParams.serviceId) : null;

  if (!service) {
    return (
      <div className="min-h-screen bg-[#0E0E10] text-[#EBEBEB] py-20 px-4 text-center space-y-4">
        <p className="text-lg">Service not found.</p>
        <button
          onClick={() => navigateTo('services')}
          className="px-6 py-2.5 rounded-xl bg-[#D4AF37] text-black font-bold text-xs"
        >
          Back to Services
        </button>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#0E0E10] text-[#EBEBEB] py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-5xl mx-auto space-y-10">
        {/* Back Button */}
        <button
          onClick={goBack}
          className="inline-flex items-center space-x-2 text-xs font-semibold text-[#EBEBEB]/70 hover:text-[#D4AF37] transition"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Back to Services</span>
        </button>

        {/* Hero image and title card */}
        <div className="relative rounded-3xl overflow-hidden aspect-[16/9] sm:aspect-[21/9] bg-black shadow-2xl border border-[#D4AF37]/30">
          <img
            src={service.imageUrl}
            alt={service.name}
            className="w-full h-full object-cover"
          />
          <div className="absolute inset-0 bg-gradient-to-t from-black via-black/40 to-transparent" />
          <div className="absolute bottom-6 sm:bottom-10 left-6 sm:left-10 right-6 sm:right-10 flex flex-col sm:flex-row sm:items-end justify-between gap-4">
            <div className="space-y-2">
              <span className="inline-block px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider bg-[#D4AF37] text-black">
                Signature Service
              </span>
              <h1 className="font-serif text-3xl sm:text-5xl font-bold text-white leading-tight">
                {service.name}
              </h1>
            </div>
            <div className="p-4 rounded-2xl bg-black/70 backdrop-blur-md border border-[#D4AF37]/40 shrink-0">
              <p className="text-[10px] text-[#EBEBEB]/60 uppercase tracking-widest">Pricing</p>
              <p className="font-serif text-2xl font-bold text-[#D4AF37]">
                Starting {service.startingPrice}
              </p>
            </div>
          </div>
        </div>

        {/* Two-Column Details */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-10">
          {/* Main narrative */}
          <div className="lg:col-span-2 space-y-8">
            <div className="p-8 rounded-2xl bg-[#141416] border border-white/5 space-y-4">
              <h2 className="font-serif text-2xl font-bold text-white">
                Service Overview
              </h2>
              <p className="text-sm text-[#EBEBEB]/80 font-light leading-relaxed whitespace-pre-line">
                {service.description}
              </p>
            </div>

            <div className="p-8 rounded-2xl bg-[#141416] border border-white/5 space-y-6">
              <h3 className="font-serif text-xl font-bold text-white">
                What's Included in This Experience
              </h3>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {service.features.map((feat, idx) => (
                  <div key={idx} className="flex items-start space-x-3 p-3 rounded-xl bg-white/[0.02] border border-white/5">
                    <CheckCircle2 className="w-4 h-4 text-[#D4AF37] mt-0.5 shrink-0" />
                    <span className="text-xs text-[#EBEBEB]/90 font-medium">{feat}</span>
                  </div>
                ))}
              </div>
            </div>

            {service.suitableEventTypes.length > 0 && (
              <div className="p-6 rounded-2xl bg-[#141416] border border-white/5 space-y-3">
                <h4 className="text-xs font-bold uppercase tracking-wider text-[#D4AF37]">
                  Suitable Event Types
                </h4>
                <div className="flex flex-wrap gap-2">
                  {service.suitableEventTypes.map((type, idx) => (
                    <span
                      key={idx}
                      className="px-3.5 py-1.5 rounded-full text-xs font-medium bg-white/5 border border-white/10 text-[#EBEBEB]/80"
                    >
                      {type}
                    </span>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* Sidebar CTA & Guarantees */}
          <div className="space-y-6">
            <div className="p-6 rounded-2xl bg-gradient-to-b from-[#1C1C1F] to-[#141416] border border-[#D4AF37]/30 shadow-xl space-y-6">
              <div className="space-y-2">
                <span className="text-xs text-[#D4AF37] font-semibold uppercase tracking-wider">
                  Reserve Your Date
                </span>
                <h3 className="font-serif text-2xl font-bold text-white">
                  Ready to Book {service.name}?
                </h3>
                <p className="text-xs text-[#EBEBEB]/70 font-light leading-relaxed">
                  Inquire today to check date availability and receive a customized quote.
                </p>
              </div>

              <button
                onClick={() => navigateTo('booking', { serviceId: service.id })}
                className="w-full py-4 rounded-xl bg-[#D4AF37] hover:bg-[#AA8C2C] text-black font-bold text-xs uppercase tracking-wider shadow-lg shadow-[#D4AF37]/20 transition flex items-center justify-center space-x-2"
              >
                <Calendar className="w-4 h-4" />
                <span>Book This Service</span>
              </button>

              <div className="space-y-3 pt-4 border-t border-white/10 text-xs text-[#EBEBEB]/70">
                <div className="flex items-center space-x-2">
                  <ShieldCheck className="w-4 h-4 text-[#D4AF37]" />
                  <span>Verified Contract & Secure Payments</span>
                </div>
                <div className="flex items-center space-x-2">
                  <Sparkles className="w-4 h-4 text-[#D4AF37]" />
                  <span>Private 4K Deliverables Gallery</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
