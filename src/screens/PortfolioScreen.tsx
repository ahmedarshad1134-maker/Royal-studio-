import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { PortfolioCategory, PortfolioItem } from '../types';
import { Camera, Film, X, Calendar, Sparkles, Filter, ChevronRight } from 'lucide-react';

export const PortfolioScreen: React.FC = () => {
  const { portfolio, navigateTo } = useApp();
  const [selectedCategory, setSelectedCategory] = useState<PortfolioCategory>('ALL');
  const [selectedPreview, setSelectedPreview] = useState<PortfolioItem | null>(null);

  const categories: { id: PortfolioCategory; label: string }[] = [
    { id: 'ALL', label: 'All Works' },
    { id: 'WEDDING', label: 'Weddings' },
    { id: 'ENGAGEMENT', label: 'Engagements' },
    { id: 'PRE_WEDDING', label: 'Pre-Wedding' },
    { id: 'BIRTHDAY', label: 'Birthdays' },
    { id: 'BABY_FAMILY', label: 'Baby & Family' },
    { id: 'ANNIVERSARY', label: 'Anniversary' },
    { id: 'OTHER_EVENTS', label: 'Galas & Events' },
  ];

  const filteredItems = portfolio
    .filter((item) => item.enabled)
    .filter((item) => selectedCategory === 'ALL' || item.category === selectedCategory);

  return (
    <div className="min-h-screen bg-[#0E0E10] text-[#EBEBEB] py-16 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto space-y-12">
        {/* Header */}
        <div className="text-center max-w-3xl mx-auto space-y-4">
          <div className="inline-flex items-center space-x-2 px-3 py-1 rounded-full bg-[#D4AF37]/15 border border-[#D4AF37]/30 text-[#D4AF37] text-xs font-semibold uppercase tracking-widest">
            <Sparkles className="w-3.5 h-3.5" />
            <span>Master Visual Archive</span>
          </div>
          <h1 className="font-serif text-4xl sm:text-6xl font-bold text-white tracking-tight">
            Curated Portfolio
          </h1>
          <p className="text-sm sm:text-base text-[#EBEBEB]/70 font-light leading-relaxed">
            Immerse yourself in our cinematic wedding films, emotional pre-wedding portraits, and regal celebration memories.
          </p>
        </div>

        {/* Filter Category Tabs */}
        <div className="flex items-center justify-center flex-wrap gap-2 pt-2">
          {categories.map((cat) => (
            <button
              key={cat.id}
              onClick={() => setSelectedCategory(cat.id)}
              className={`px-4 py-2 rounded-full text-xs font-medium transition ${
                selectedCategory === cat.id
                  ? 'bg-[#D4AF37] text-black font-bold shadow-lg shadow-[#D4AF37]/20'
                  : 'bg-white/5 text-[#EBEBEB]/70 hover:bg-white/10 hover:text-white border border-white/5'
              }`}
            >
              {cat.label}
            </button>
          ))}
        </div>

        {/* Gallery Grid */}
        {filteredItems.length === 0 ? (
          <div className="text-center py-20 bg-white/[0.02] rounded-3xl border border-white/5">
            <p className="text-base text-[#EBEBEB]/60">No portfolio works found in this category.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredItems.map((item) => (
              <div
                key={item.id}
                onClick={() => setSelectedPreview(item)}
                className="group relative rounded-2xl overflow-hidden aspect-[4/3] bg-[#141416] border border-white/5 hover:border-[#D4AF37]/40 cursor-pointer shadow-xl transition-all duration-300 hover:-translate-y-1"
              >
                <img
                  src={item.imageUrl}
                  alt={item.title}
                  className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
                />
                <div className="absolute inset-0 bg-gradient-to-t from-black/90 via-black/30 to-transparent opacity-60 group-hover:opacity-90 transition-opacity" />

                {/* Top Badge */}
                <div className="absolute top-4 left-4 flex items-center space-x-2">
                  <span className="px-2.5 py-1 rounded-full text-[10px] font-bold tracking-wider uppercase bg-black/60 backdrop-blur-md text-[#D4AF37] border border-[#D4AF37]/30">
                    {item.category.replace('_', ' ')}
                  </span>
                  {item.featured && (
                    <span className="px-2 py-0.5 rounded-full text-[9px] font-bold uppercase bg-[#D4AF37] text-black">
                      Featured
                    </span>
                  )}
                </div>

                {/* Bottom Content */}
                <div className="absolute bottom-0 inset-x-0 p-5 space-y-1">
                  <h3 className="font-serif text-lg font-bold text-white leading-snug">
                    {item.title}
                  </h3>
                  <div className="flex items-center space-x-2 text-xs text-[#EBEBEB]/70">
                    {item.type === 'VIDEO' ? (
                      <Film className="w-3.5 h-3.5 text-[#D4AF37]" />
                    ) : (
                      <Camera className="w-3.5 h-3.5 text-[#D4AF37]" />
                    )}
                    <span>{item.type === 'VIDEO' ? 'Cinematic Video' : 'Editorial Photography'}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* CTA Banner */}
        <div className="mt-16 p-8 sm:p-12 rounded-3xl bg-gradient-to-r from-[#18181A] to-[#222227] border border-[#D4AF37]/30 flex flex-col md:flex-row items-center justify-between gap-6">
          <div className="space-y-2 text-center md:text-left">
            <h3 className="font-serif text-2xl sm:text-3xl font-bold text-white">
              Envisioning Your Special Day With Us?
            </h3>
            <p className="text-xs sm:text-sm text-[#EBEBEB]/70">
              Let's create timeless visuals tailored to your unique love story.
            </p>
          </div>
          <button
            onClick={() => navigateTo('booking')}
            className="px-8 py-3.5 rounded-xl bg-[#D4AF37] text-black font-bold text-xs uppercase tracking-wider hover:bg-[#AA8C2C] transition shrink-0"
          >
            Check Date Availability
          </button>
        </div>
      </div>

      {/* Lightbox Preview Modal */}
      {selectedPreview && (
        <div
          onClick={() => setSelectedPreview(null)}
          className="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6 bg-black/90 backdrop-blur-md animate-fadeIn"
        >
          <div
            onClick={(e) => e.stopPropagation()}
            className="relative max-w-4xl w-full bg-[#18181B] rounded-2xl overflow-hidden border border-[#D4AF37]/40 shadow-2xl space-y-4 p-4 sm:p-6"
          >
            <div className="flex items-center justify-between pb-2 border-b border-white/10">
              <div>
                <span className="text-xs font-bold text-[#D4AF37] uppercase tracking-wider">
                  {selectedPreview.category.replace('_', ' ')}
                </span>
                <h3 className="font-serif text-xl sm:text-2xl font-bold text-white">
                  {selectedPreview.title}
                </h3>
              </div>
              <button
                onClick={() => setSelectedPreview(null)}
                className="p-2 rounded-full text-[#EBEBEB]/60 hover:text-white hover:bg-white/10"
              >
                <X className="w-6 h-6" />
              </button>
            </div>

            <div className="relative rounded-xl overflow-hidden aspect-video bg-black flex items-center justify-center">
              <img
                src={selectedPreview.imageUrl}
                alt={selectedPreview.title}
                className="w-full h-full object-contain"
              />
            </div>

            <div className="flex flex-col sm:flex-row items-center justify-between gap-4 pt-2">
              <div className="flex items-center space-x-3 text-xs text-[#EBEBEB]/70">
                <span className="flex items-center space-x-1">
                  {selectedPreview.type === 'VIDEO' ? (
                    <Film className="w-4 h-4 text-[#D4AF37]" />
                  ) : (
                    <Camera className="w-4 h-4 text-[#D4AF37]" />
                  )}
                  <span>{selectedPreview.type === 'VIDEO' ? '4K Cinema Edit' : 'Master Digital Print'}</span>
                </span>
                <span>•</span>
                <span>Royal Studio Fine-Art Collection</span>
              </div>

              <button
                onClick={() => {
                  setSelectedPreview(null);
                  navigateTo('booking');
                }}
                className="w-full sm:w-auto px-6 py-2.5 rounded-xl bg-[#D4AF37] hover:bg-[#AA8C2C] text-black font-bold text-xs uppercase tracking-wider transition"
              >
                Book Similar Coverage
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
