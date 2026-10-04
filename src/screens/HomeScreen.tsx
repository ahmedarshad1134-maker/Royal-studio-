import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import {
  Calendar,
  Camera,
  Film,
  Award,
  Star,
  CheckCircle2,
  ArrowRight,
  ShieldCheck,
  Sparkles,
  Users,
  Clock,
  ExternalLink,
  ChevronRight,
} from 'lucide-react';
import { PortfolioCategory } from '../types';

export const HomeScreen: React.FC = () => {
  const { navigateTo, currentUser, services, portfolio, reviews, settings } = useApp();
  const [selectedCategory, setSelectedCategory] = useState<PortfolioCategory>('ALL');

  const filteredPortfolio = portfolio
    .filter((item) => item.enabled)
    .filter((item) => selectedCategory === 'ALL' || item.category === selectedCategory)
    .slice(0, 6);

  const featuredReviews = reviews.filter((r) => r.approved && r.featured).slice(0, 3);
  const displayReviews = featuredReviews.length > 0 ? featuredReviews : reviews.filter((r) => r.approved).slice(0, 3);

  return (
    <div className="min-h-screen bg-[#0E0E10] text-[#EBEBEB]">
      {/* 1. Hero Section */}
      <section className="relative min-h-[85vh] lg:min-h-[90vh] flex items-center justify-center overflow-hidden">
        {/* Background Image with dark luxury gradient */}
        <div className="absolute inset-0">
          <img
            src="/assets/images/hero_wedding_cinematic_1789061882938.jpg"
            alt="Royal Studio Wedding Cinema"
            className="w-full h-full object-cover object-center transform scale-105 transition-transform duration-1000"
          />
          <div className="absolute inset-0 bg-gradient-to-t from-[#0E0E10] via-[#0E0E10]/70 to-[#0E0E10]/40" />
          <div className="absolute inset-0 bg-radial-at-c from-transparent via-[#0E0E10]/50 to-[#0E0E10]" />
        </div>

        <div className="relative z-10 max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-20 text-center space-y-6">
          <div className="inline-flex items-center space-x-2 px-4 py-1.5 rounded-full bg-[#D4AF37]/15 border border-[#D4AF37]/40 text-[#D4AF37] text-xs font-semibold tracking-widest uppercase animate-fadeIn">
            <Sparkles className="w-3.5 h-3.5" />
            <span>Luxury Wedding Photography & Cinematography</span>
          </div>

          <h1 className="font-serif text-4xl sm:text-6xl lg:text-7xl font-bold tracking-tight text-white leading-tight">
            Preserving Your Most <br />
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-[#D4AF37] via-[#F3E5AB] to-[#AA8C2C]">
              Regal Memories
            </span>
          </h1>

          <p className="max-w-2xl mx-auto text-base sm:text-lg text-[#EBEBEB]/80 font-light leading-relaxed">
            {settings.tagline || 'Timeless & Royal Visual Experiences'}. Specializing in high-end weddings, bespoke engagements, milestone celebrations, and heirloom cinema.
          </p>

          <div className="flex flex-col sm:flex-row items-center justify-center gap-4 pt-4">
            <button
              onClick={() => navigateTo('booking')}
              className="w-full sm:w-auto px-8 py-4 rounded-xl bg-gradient-to-r from-[#D4AF37] to-[#AA8C2C] text-black font-bold text-sm tracking-wide shadow-xl shadow-[#D4AF37]/25 hover:brightness-110 active:scale-95 transition flex items-center justify-center space-x-2"
            >
              <Calendar className="w-4 h-4" />
              <span>Book Your Date</span>
            </button>
            <button
              onClick={() => navigateTo('portfolio')}
              className="w-full sm:w-auto px-8 py-4 rounded-xl bg-[#1C1C1E]/80 hover:bg-[#2C2C2E] border border-white/10 hover:border-[#D4AF37]/40 text-white font-semibold text-sm transition flex items-center justify-center space-x-2"
            >
              <Camera className="w-4 h-4 text-[#D4AF37]" />
              <span>Explore Portfolio</span>
            </button>
          </div>

          {/* Key Trust Badges */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 pt-12 max-w-3xl mx-auto text-left">
            {[
              { label: 'Cinematic Masterclass', desc: '4K Cinema & Dual Audio' },
              { label: 'Archival Heirlooms', desc: 'Italian Leather Albums' },
              { label: 'Licensed Drone Crew', desc: 'FAA & Local Air Permits' },
              { label: 'Express Delivery', desc: 'Priority Delivery Suite' },
            ].map((badge, idx) => (
              <div key={idx} className="p-3 rounded-xl bg-white/[0.03] border border-white/5 backdrop-blur-sm">
                <CheckCircle2 className="w-4 h-4 text-[#D4AF37] mb-1" />
                <p className="text-xs font-bold text-white">{badge.label}</p>
                <p className="text-[10px] text-[#EBEBEB]/60">{badge.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* 2. Client Portal Quick Entry Banner */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 -mt-6 relative z-20">
        <div className="p-5 sm:p-6 rounded-2xl bg-gradient-to-r from-[#18181A] to-[#202024] border border-[#D4AF37]/30 shadow-2xl flex flex-col md:flex-row items-center justify-between gap-4">
          <div className="flex items-center space-x-4">
            <div className="w-12 h-12 rounded-xl bg-[#D4AF37]/15 border border-[#D4AF37]/30 flex items-center justify-center text-[#D4AF37] shrink-0">
              <ShieldCheck className="w-6 h-6" />
            </div>
            <div>
              <h3 className="font-serif text-lg font-bold text-white">
                {currentUser ? `Welcome Back, ${currentUser.name}` : 'Royal Studio Client Portal'}
              </h3>
              <p className="text-xs text-[#EBEBEB]/70">
                {currentUser
                  ? 'Access your active bookings, contracts, timeline updates, and private delivery gallery.'
                  : 'Track your event deliverables, download invoices, and preview your private gallery.'}
              </p>
            </div>
          </div>
          <div className="flex items-center space-x-3 shrink-0 w-full md:w-auto">
            <button
              onClick={() => navigateTo('customer_area')}
              className="flex-1 md:flex-none px-6 py-2.5 rounded-xl bg-[#D4AF37] text-black font-bold text-xs hover:bg-[#AA8C2C] transition flex items-center justify-center space-x-2"
            >
              <span>{currentUser ? 'Open My Dashboard' : 'Sign In to Client Area'}</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </button>
            <button
              onClick={() => navigateTo('booking')}
              className="px-4 py-2.5 rounded-xl border border-white/20 text-[#EBEBEB] hover:bg-white/5 font-medium text-xs transition"
            >
              New Enquiry
            </button>
          </div>
        </div>
      </section>

      {/* 3. Signature Services Section */}
      <section className="py-24 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-3xl mx-auto mb-16 space-y-3">
          <span className="text-xs uppercase tracking-widest text-[#D4AF37] font-semibold">
            Our Offerings
          </span>
          <h2 className="font-serif text-3xl sm:text-5xl font-bold text-white">
            Signature Visual Services
          </h2>
          <p className="text-sm text-[#EBEBEB]/70 font-light leading-relaxed">
            Tailored visual storytelling crafted with cinematic lenses, editorial composition, and thoughtful client care.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {services
            .filter((s) => s.enabled)
            .slice(0, 6)
            .map((svc) => (
              <div
                key={svc.id}
                onClick={() => navigateTo('service_detail', { serviceId: svc.id })}
                className="group relative rounded-2xl bg-[#141416] border border-white/5 hover:border-[#D4AF37]/50 overflow-hidden transition-all duration-300 hover:-translate-y-1 hover:shadow-2xl hover:shadow-[#D4AF37]/10 flex flex-col cursor-pointer"
              >
                <div className="relative h-60 overflow-hidden bg-black/40">
                  <img
                    src={svc.imageUrl}
                    alt={svc.name}
                    className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-[#141416] via-transparent to-transparent" />
                  <div className="absolute top-4 right-4 px-3 py-1 rounded-full bg-black/70 backdrop-blur-md border border-[#D4AF37]/30 text-[#D4AF37] text-xs font-bold">
                    From {svc.startingPrice}
                  </div>
                </div>

                <div className="p-6 flex-1 flex flex-col justify-between space-y-4">
                  <div className="space-y-2">
                    <h3 className="font-serif text-xl font-bold text-white group-hover:text-[#D4AF37] transition-colors">
                      {svc.name}
                    </h3>
                    <p className="text-xs text-[#EBEBEB]/70 line-clamp-2 leading-relaxed">
                      {svc.shortDescription}
                    </p>
                  </div>

                  <div className="space-y-2 pt-2 border-t border-white/5">
                    {svc.features.slice(0, 3).map((feat, fIdx) => (
                      <div key={fIdx} className="flex items-center space-x-2 text-xs text-[#EBEBEB]/60">
                        <CheckCircle2 className="w-3.5 h-3.5 text-[#D4AF37] shrink-0" />
                        <span className="truncate">{feat}</span>
                      </div>
                    ))}
                  </div>

                  <div className="pt-2 flex items-center justify-between text-xs font-semibold text-[#D4AF37] group-hover:translate-x-1 transition-transform">
                    <span>Explore Service Details</span>
                    <ChevronRight className="w-4 h-4" />
                  </div>
                </div>
              </div>
            ))}
        </div>

        <div className="mt-12 text-center">
          <button
            onClick={() => navigateTo('services')}
            className="px-8 py-3 rounded-xl border border-[#D4AF37]/40 text-[#D4AF37] hover:bg-[#D4AF37]/10 text-xs font-bold uppercase tracking-wider transition"
          >
            View All Services & Add-Ons
          </button>
        </div>
      </section>

      {/* 4. Featured Portfolio Gallery */}
      <section className="py-20 bg-[#121214] border-y border-white/5">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex flex-col md:flex-row md:items-end justify-between mb-12 gap-6">
            <div className="space-y-2">
              <span className="text-xs uppercase tracking-widest text-[#D4AF37] font-semibold">
                Captured Moments
              </span>
              <h2 className="font-serif text-3xl sm:text-4xl font-bold text-white">
                Featured Portfolio
              </h2>
            </div>

            {/* Category Filter Pills */}
            <div className="flex items-center space-x-2 overflow-x-auto pb-2 scrollbar-none">
              {(['ALL', 'WEDDING', 'ENGAGEMENT', 'PRE_WEDDING', 'BIRTHDAY'] as PortfolioCategory[]).map(
                (cat) => (
                  <button
                    key={cat}
                    onClick={() => setSelectedCategory(cat)}
                    className={`px-3.5 py-1.5 rounded-full text-xs font-medium transition shrink-0 ${
                      selectedCategory === cat
                        ? 'bg-[#D4AF37] text-black font-semibold'
                        : 'bg-white/5 text-[#EBEBEB]/70 hover:bg-white/10 hover:text-white'
                    }`}
                  >
                    {cat.replace('_', ' ')}
                  </button>
                )
              )}
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredPortfolio.map((item) => (
              <div
                key={item.id}
                onClick={() => navigateTo('portfolio')}
                className="group relative rounded-2xl overflow-hidden aspect-[4/3] bg-black cursor-pointer shadow-lg"
              >
                <img
                  src={item.imageUrl}
                  alt={item.title}
                  className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"
                />
                <div className="absolute inset-0 bg-gradient-to-t from-black/90 via-black/30 to-transparent opacity-70 group-hover:opacity-95 transition-opacity" />

                <div className="absolute bottom-0 inset-x-0 p-5 space-y-1 transform translate-y-2 group-hover:translate-y-0 transition-transform">
                  <span className="inline-block text-[10px] uppercase font-bold text-[#D4AF37] tracking-wider">
                    {item.category.replace('_', ' ')}
                  </span>
                  <h4 className="font-serif text-lg font-bold text-white leading-snug">
                    {item.title}
                  </h4>
                  <div className="flex items-center space-x-2 pt-1 text-xs text-[#EBEBEB]/70 opacity-0 group-hover:opacity-100 transition-opacity">
                    {item.type === 'VIDEO' ? (
                      <Film className="w-3.5 h-3.5 text-[#D4AF37]" />
                    ) : (
                      <Camera className="w-3.5 h-3.5 text-[#D4AF37]" />
                    )}
                    <span>{item.type === 'VIDEO' ? 'Cinematic Video' : 'Fine-Art Photograph'}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>

          <div className="mt-12 text-center">
            <button
              onClick={() => navigateTo('portfolio')}
              className="px-8 py-3 rounded-xl bg-white/5 hover:bg-white/10 text-white font-semibold text-xs border border-white/10 hover:border-[#D4AF37]/50 transition"
            >
              Open Full Portfolio Gallery
            </button>
          </div>
        </div>
      </section>

      {/* 5. Why Choose Royal Studio */}
      <section className="py-24 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-16 items-center">
          <div className="space-y-6">
            <span className="text-xs uppercase tracking-widest text-[#D4AF37] font-semibold">
              The Royal Standard
            </span>
            <h2 className="font-serif text-3xl sm:text-5xl font-bold text-white leading-tight">
              Why Couples & Brands Entrust Their Legacy To Us
            </h2>
            <p className="text-sm text-[#EBEBEB]/80 font-light leading-relaxed">
              We approach each event not just as technical operators, but as fine-art documentarians. From custom color science to emotional timing, our craft is designed to transcend passing trends.
            </p>

            <div className="space-y-4 pt-2">
              {[
                {
                  icon: Camera,
                  title: 'Cinema-Grade 4K & Dual Audio Recording',
                  desc: 'Industry-standard cinema bodies, prime lenses, and discreet multi-mic setups capturing vows with flawless clarity.',
                },
                {
                  icon: Sparkles,
                  title: 'Handcrafted Bespoke Color Grading',
                  desc: 'Every photograph and video reel passes through our master post-production suite for regal color depth and skin tones.',
                },
                {
                  icon: ShieldCheck,
                  title: 'Encrypted Cloud Deliverables & Invoices',
                  desc: 'Private client portals with 4K downloads, sharable web galleries, and transparent online account tracking.',
                },
              ].map((feat, idx) => (
                <div key={idx} className="flex items-start space-x-4 p-4 rounded-xl bg-white/[0.02] border border-white/5">
                  <div className="p-2.5 rounded-lg bg-[#D4AF37]/10 text-[#D4AF37] shrink-0 mt-0.5">
                    <feat.icon className="w-5 h-5" />
                  </div>
                  <div>
                    <h4 className="text-sm font-bold text-white">{feat.title}</h4>
                    <p className="text-xs text-[#EBEBEB]/70 mt-1 leading-relaxed">{feat.desc}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="relative">
            <div className="relative rounded-3xl overflow-hidden border border-[#D4AF37]/30 shadow-2xl">
              <img
                src="/assets/images/service_wedding_1789061903404.jpg"
                alt="Studio Craft"
                className="w-full h-full object-cover"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-black via-transparent to-transparent" />
              <div className="absolute bottom-6 left-6 right-6 p-6 rounded-2xl bg-black/80 backdrop-blur-md border border-[#D4AF37]/30">
                <div className="flex items-center space-x-4">
                  <div className="w-12 h-12 rounded-full border-2 border-[#D4AF37] p-0.5">
                    <img
                      src="/assets/images/app_icon_foreground_1789656291104.jpg"
                      alt="Badge"
                      className="w-full h-full object-cover rounded-full"
                    />
                  </div>
                  <div>
                    <p className="font-serif text-lg font-bold text-white">{settings.studioName}</p>
                    <p className="text-xs text-[#D4AF37]">Over a Decade of Visual Excellence</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* 6. Client Reviews Spotlight */}
      <section className="py-20 bg-[#121214] border-t border-white/5">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-2xl mx-auto mb-16 space-y-2">
            <span className="text-xs uppercase tracking-widest text-[#D4AF37] font-semibold">
              Words of Love
            </span>
            <h2 className="font-serif text-3xl sm:text-4xl font-bold text-white">
              What Our Clients Say
            </h2>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {displayReviews.map((rev) => (
              <div
                key={rev.id}
                className="p-8 rounded-2xl bg-[#18181B] border border-white/5 hover:border-[#D4AF37]/30 flex flex-col justify-between transition-all"
              >
                <div className="space-y-4">
                  <div className="flex items-center space-x-1 text-[#D4AF37]">
                    {Array.from({ length: rev.rating }).map((_, i) => (
                      <Star key={i} className="w-4 h-4 fill-[#D4AF37]" />
                    ))}
                  </div>
                  <p className="text-sm text-[#EBEBEB]/80 font-light italic leading-relaxed">
                    "{rev.review}"
                  </p>
                </div>

                <div className="pt-6 border-t border-white/5 mt-6 flex items-center justify-between">
                  <div>
                    <h4 className="text-sm font-bold text-white">{rev.customerName}</h4>
                    <p className="text-xs text-[#D4AF37]">{rev.eventType}</p>
                  </div>
                  <span className="text-[10px] text-[#EBEBEB]/40">
                    {new Date(rev.createdAt).toLocaleDateString([], {
                      month: 'short',
                      year: 'numeric',
                    })}
                  </span>
                </div>
              </div>
            ))}
          </div>

          <div className="mt-12 text-center">
            <button
              onClick={() => navigateTo('reviews')}
              className="px-8 py-3 rounded-xl border border-[#D4AF37]/40 text-[#D4AF37] hover:bg-[#D4AF37]/10 text-xs font-bold uppercase tracking-wider transition"
            >
              Read All Reviews / Submit Yours
            </button>
          </div>
        </div>
      </section>

      {/* 7. Booking Call to Action */}
      <section className="py-20 relative overflow-hidden">
        <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="rounded-3xl p-8 sm:p-14 bg-gradient-to-r from-[#18181A] via-[#202025] to-[#18181A] border border-[#D4AF37]/40 text-center space-y-6 shadow-2xl relative">
            <h2 className="font-serif text-3xl sm:text-5xl font-bold text-white leading-tight">
              Dates Fill Quickly for the Season.
              <br />
              <span className="text-[#D4AF37]">Reserve Your Date Today.</span>
            </h2>
            <p className="max-w-xl mx-auto text-sm text-[#EBEBEB]/80 font-light">
              We take a limited number of events each year to ensure uncompromising dedication to every couple and client. Check your date availability immediately.
            </p>
            <div className="pt-4 flex flex-col sm:flex-row items-center justify-center gap-4">
              <button
                onClick={() => navigateTo('booking')}
                className="w-full sm:w-auto px-10 py-4 rounded-xl bg-[#D4AF37] hover:bg-[#AA8C2C] text-black font-bold text-sm tracking-wide shadow-xl shadow-[#D4AF37]/30 transition"
              >
                Launch Booking Enquiry Wizard
              </button>
              <button
                onClick={() => navigateTo('contact')}
                className="w-full sm:w-auto px-8 py-4 rounded-xl bg-white/5 hover:bg-white/10 text-white font-medium text-sm border border-white/10 transition"
              >
                Contact Studio Directly
              </button>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};
