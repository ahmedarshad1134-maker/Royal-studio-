import React from 'react';
import { useApp } from '../context/AppContext';
import { ArrowLeft, CheckCircle2, Clock, Users, BookOpen, Truck, PlusCircle, Calendar, ShieldCheck } from 'lucide-react';

export const PackageDetailScreen: React.FC = () => {
  const { screenParams, getPackageById, navigateTo, goBack } = useApp();
  const pkg = screenParams.packageId ? getPackageById(screenParams.packageId) : null;

  if (!pkg) {
    return (
      <div className="min-h-screen bg-[#0E0E10] text-[#EBEBEB] py-20 px-4 text-center space-y-4">
        <p className="text-lg">Package not found.</p>
        <button
          onClick={() => navigateTo('packages')}
          className="px-6 py-2.5 rounded-xl bg-[#D4AF37] text-black font-bold text-xs"
        >
          Back to Packages
        </button>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#0E0E10] text-[#EBEBEB] py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto space-y-10">
        {/* Back Button */}
        <button
          onClick={goBack}
          className="inline-flex items-center space-x-2 text-xs font-semibold text-[#EBEBEB]/70 hover:text-[#D4AF37] transition"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Back to Packages</span>
        </button>

        {/* Top Header Card */}
        <div className="p-8 sm:p-12 rounded-3xl bg-gradient-to-r from-[#18181A] to-[#202025] border border-[#D4AF37]/40 shadow-2xl flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div className="space-y-3">
            {pkg.isRecommended && (
              <span className="inline-block px-3 py-1 rounded-full text-[10px] font-extrabold uppercase tracking-widest bg-[#D4AF37] text-black">
                Most Popular Choice
              </span>
            )}
            <h1 className="font-serif text-3xl sm:text-5xl font-bold text-white">
              {pkg.name}
            </h1>
            <p className="text-sm text-[#EBEBEB]/80 font-light max-w-lg leading-relaxed">
              {pkg.description}
            </p>
          </div>

          <div className="p-6 rounded-2xl bg-black/60 border border-[#D4AF37]/30 text-center shrink-0">
            <span className="text-xs text-[#D4AF37] uppercase tracking-wider block">Investment</span>
            <div className="flex items-baseline justify-center space-x-1 my-1">
              <span className="text-lg font-semibold text-[#D4AF37]">{pkg.currency}</span>
              <span className="font-serif text-4xl sm:text-5xl font-bold text-white">
                {pkg.price}
              </span>
            </div>
            <p className="text-[11px] text-[#EBEBEB]/50">All-Inclusive Visual Suite</p>
          </div>
        </div>

        {/* Technical Specs Breakdown */}
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4">
          <div className="p-5 rounded-2xl bg-[#141416] border border-white/5 space-y-1">
            <Clock className="w-5 h-5 text-[#D4AF37] mb-2" />
            <p className="text-xs text-[#EBEBEB]/60 uppercase tracking-wider">Photo Hours</p>
            <p className="text-sm font-bold text-white">{pkg.photographyHours}</p>
          </div>
          <div className="p-5 rounded-2xl bg-[#141416] border border-white/5 space-y-1">
            <Clock className="w-5 h-5 text-[#D4AF37] mb-2" />
            <p className="text-xs text-[#EBEBEB]/60 uppercase tracking-wider">Cinema Hours</p>
            <p className="text-sm font-bold text-white">{pkg.videographyHours}</p>
          </div>
          <div className="p-5 rounded-2xl bg-[#141416] border border-white/5 space-y-1">
            <Users className="w-5 h-5 text-[#D4AF37] mb-2" />
            <p className="text-xs text-[#EBEBEB]/60 uppercase tracking-wider">Photographers</p>
            <p className="text-sm font-bold text-white">{pkg.numberOfPhotographers} Shooter(s)</p>
          </div>
          <div className="p-5 rounded-2xl bg-[#141416] border border-white/5 space-y-1">
            <Users className="w-5 h-5 text-[#D4AF37] mb-2" />
            <p className="text-xs text-[#EBEBEB]/60 uppercase tracking-wider">Cinematographers</p>
            <p className="text-sm font-bold text-white">{pkg.numberOfVideographers} Shooter(s)</p>
          </div>
        </div>

        {/* Deliverables & Deliveries */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="p-6 rounded-2xl bg-[#141416] border border-white/5 space-y-3">
            <div className="flex items-center space-x-2 text-[#D4AF37]">
              <BookOpen className="w-5 h-5" />
              <h3 className="font-serif text-lg font-bold text-white">Album Specification</h3>
            </div>
            <p className="text-xs sm:text-sm text-[#EBEBEB]/80 font-light leading-relaxed">
              {pkg.albumInformation}
            </p>
          </div>

          <div className="p-6 rounded-2xl bg-[#141416] border border-white/5 space-y-3">
            <div className="flex items-center space-x-2 text-[#D4AF37]">
              <Truck className="w-5 h-5" />
              <h3 className="font-serif text-lg font-bold text-white">Delivery Timeline</h3>
            </div>
            <p className="text-xs sm:text-sm text-[#EBEBEB]/80 font-light leading-relaxed">
              {pkg.deliveryInformation}
            </p>
          </div>
        </div>

        {/* Included Checklist */}
        <div className="p-8 rounded-3xl bg-[#141416] border border-white/5 space-y-6">
          <h3 className="font-serif text-2xl font-bold text-white">
            Included in the {pkg.name}
          </h3>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {pkg.includedServices.map((feat, idx) => (
              <div key={idx} className="flex items-center space-x-3 p-3.5 rounded-xl bg-white/[0.02] border border-white/5">
                <CheckCircle2 className="w-4 h-4 text-[#D4AF37] shrink-0" />
                <span className="text-xs sm:text-sm text-[#EBEBEB]/90">{feat}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Add-ons if any */}
        {pkg.optionalAddOns.length > 0 && (
          <div className="p-8 rounded-3xl bg-[#141416] border border-white/5 space-y-4">
            <h3 className="font-serif text-xl font-bold text-white">
              Optional Enhancements & Add-Ons
            </h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              {pkg.optionalAddOns.map((addon, idx) => (
                <div key={idx} className="flex items-center space-x-2.5 p-3 rounded-xl bg-white/[0.02] border border-white/5 text-xs text-[#EBEBEB]/80">
                  <PlusCircle className="w-4 h-4 text-[#D4AF37] shrink-0" />
                  <span>{addon}</span>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Bottom Booking CTA */}
        <div className="p-8 rounded-3xl bg-gradient-to-r from-[#1F1F24] to-[#141416] border border-[#D4AF37]/30 text-center space-y-4">
          <h3 className="font-serif text-2xl font-bold text-white">
            Ready to Lock In {pkg.name}?
          </h3>
          <p className="text-xs text-[#EBEBEB]/70 max-w-md mx-auto">
            Proceed to our booking enquiry wizard to verify your event date and customize any extras.
          </p>
          <button
            onClick={() => navigateTo('booking', { packageId: pkg.id })}
            className="px-10 py-4 rounded-xl bg-[#D4AF37] hover:bg-[#AA8C2C] text-black font-bold text-xs uppercase tracking-wider shadow-xl shadow-[#D4AF37]/20 transition inline-flex items-center space-x-2"
          >
            <Calendar className="w-4 h-4" />
            <span>Proceed with {pkg.name}</span>
          </button>
        </div>
      </div>
    </div>
  );
};
