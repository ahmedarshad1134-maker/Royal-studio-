import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { ArrowLeft, Download, Eye, Sparkles, X, Shield, Lock, Film, Camera } from 'lucide-react';

export const CustomerPrivateGalleryScreen: React.FC = () => {
  const { screenParams, getBookingById, currentUser, navigateTo, goBack } = useApp();
  const booking = screenParams.bookingId ? getBookingById(screenParams.bookingId) : null;
  const [selectedImage, setSelectedImage] = useState<string | null>(null);

  // Authorization check
  const isOwner =
    currentUser &&
    booking &&
    (booking.customerId === currentUser.uid ||
      booking.email.toLowerCase() === currentUser.email.toLowerCase() ||
      currentUser.role === 'admin');

  if (!currentUser) {
    return (
      <div className="min-h-screen bg-[#0E0E10] text-[#EBEBEB] py-20 px-4 text-center space-y-4">
        <Lock className="w-12 h-12 text-[#D4AF37] mx-auto" />
        <h2 className="font-serif text-2xl font-bold text-white">Private Gallery Protected</h2>
        <p className="text-xs text-[#EBEBEB]/70 max-w-sm mx-auto">
          Please sign in to your client account to view secure media deliverables.
        </p>
        <button
          onClick={() => navigateTo('customer_area')}
          className="px-6 py-2.5 rounded-xl bg-[#D4AF37] text-black font-bold text-xs"
        >
          Sign In
        </button>
      </div>
    );
  }

  if (booking && !isOwner) {
    return (
      <div className="min-h-screen bg-[#0E0E10] text-[#EBEBEB] py-20 px-4 text-center space-y-4">
        <Shield className="w-12 h-12 text-red-400 mx-auto" />
        <h2 className="font-serif text-2xl font-bold text-white">Access Restricted</h2>
        <p className="text-xs text-[#EBEBEB]/70 max-w-sm mx-auto">
          You do not have permission to view another customer's private deliverables.
        </p>
        <button
          onClick={() => navigateTo('customer_area')}
          className="px-6 py-2.5 rounded-xl bg-white/10 text-white font-bold text-xs"
        >
          Return to My Client Area
        </button>
      </div>
    );
  }

  // Deliverables media images
  const galleryMedia = [
    {
      id: 'img_1',
      title: 'Ceremony Vows Exchange - 4K Master',
      url: '/assets/images/hero_wedding_cinematic_1789061882938.jpg',
      category: 'Ceremony',
      size: '24.2 MB RAW',
    },
    {
      id: 'img_2',
      title: 'Rings & Fine Jewelry Macro',
      url: '/assets/images/service_wedding_1789061903404.jpg',
      category: 'Details',
      size: '18.7 MB RAW',
    },
    {
      id: 'img_3',
      title: 'Sunset Golden Hour Portrait',
      url: '/assets/images/portfolio_sample_1789061922601.jpg',
      category: 'Portraits',
      size: '22.1 MB RAW',
    },
    {
      id: 'img_4',
      title: 'Grand Entrance & Celebration Toast',
      url: '/assets/images/service_event_1789061936720.jpg',
      category: 'Reception',
      size: '20.5 MB RAW',
    },
  ];

  return (
    <div className="min-h-screen bg-[#0E0E10] text-[#EBEBEB] py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-6xl mx-auto space-y-10">
        {/* Navigation back */}
        <div className="flex items-center justify-between">
          <button
            onClick={() => navigateTo('customer_area')}
            className="inline-flex items-center space-x-2 text-xs font-semibold text-[#EBEBEB]/70 hover:text-[#D4AF37] transition"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Back to Client Area</span>
          </button>
          <span className="text-xs font-mono text-[#D4AF37]">
            Booking #{booking?.referenceId || 'ROYAL-2026'}
          </span>
        </div>

        {/* Gallery Title & Header */}
        <div className="p-8 sm:p-10 rounded-3xl bg-gradient-to-r from-[#18181A] to-[#202025] border border-[#D4AF37]/40 shadow-2xl flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div className="space-y-2">
            <div className="inline-flex items-center space-x-2 px-3 py-1 rounded-full bg-[#D4AF37]/20 border border-[#D4AF37]/30 text-[#D4AF37] text-[10px] font-bold uppercase tracking-widest">
              <Sparkles className="w-3.5 h-3.5" />
              <span>Full Resolution Deliverables</span>
            </div>
            <h1 className="font-serif text-3xl sm:text-4xl font-bold text-white">
              Private Media Deliverables
            </h1>
            <p className="text-xs sm:text-sm text-[#EBEBEB]/70 font-light">
              Curated master deliverables for {booking?.customerName || currentUser.name} ({booking?.eventType || 'Private Event'}).
            </p>
          </div>

          <div className="shrink-0 flex items-center space-x-3">
            <button
              onClick={() => {
                alert('Downloading full high-resolution ZIP bundle (1.4 GB)...');
              }}
              className="px-6 py-3 rounded-xl bg-[#D4AF37] hover:bg-[#AA8C2C] text-black font-bold text-xs uppercase tracking-wider transition flex items-center space-x-2 shadow-lg shadow-[#D4AF37]/20"
            >
              <Download className="w-4 h-4" />
              <span>Download Master ZIP</span>
            </button>
          </div>
        </div>

        {/* Deliverables Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-2 gap-8">
          {galleryMedia.map((media) => (
            <div
              key={media.id}
              className="rounded-2xl bg-[#141416] border border-white/5 overflow-hidden shadow-xl group flex flex-col justify-between"
            >
              <div
                onClick={() => setSelectedImage(media.url)}
                className="relative aspect-[16/10] bg-black cursor-pointer overflow-hidden"
              >
                <img
                  src={media.url}
                  alt={media.title}
                  className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
                />
                <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                  <div className="p-3 rounded-full bg-black/80 text-[#D4AF37] border border-[#D4AF37]/30 flex items-center space-x-2 text-xs font-bold">
                    <Eye className="w-4 h-4" />
                    <span>View High-Res</span>
                  </div>
                </div>
              </div>

              <div className="p-5 flex items-center justify-between gap-4">
                <div>
                  <span className="text-[10px] text-[#D4AF37] uppercase font-bold tracking-wider">
                    {media.category}
                  </span>
                  <h4 className="font-serif text-lg font-bold text-white">{media.title}</h4>
                  <p className="text-[11px] text-[#EBEBEB]/50">{media.size}</p>
                </div>

                <a
                  href={media.url}
                  download
                  className="p-2.5 rounded-xl bg-white/5 hover:bg-white/10 text-[#D4AF37] border border-white/10 hover:border-[#D4AF37]/30 transition shrink-0"
                  title="Download File"
                >
                  <Download className="w-4 h-4" />
                </a>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Lightbox Modal */}
      {selectedImage && (
        <div
          onClick={() => setSelectedImage(null)}
          className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/95 backdrop-blur-md animate-fadeIn"
        >
          <div
            onClick={(e) => e.stopPropagation()}
            className="relative max-w-5xl w-full max-h-[90vh] flex flex-col items-center"
          >
            <button
              onClick={() => setSelectedImage(null)}
              className="absolute -top-12 right-0 p-2 text-[#EBEBEB]/70 hover:text-white"
            >
              <X className="w-6 h-6" />
            </button>
            <img
              src={selectedImage}
              alt="High-Res Deliverable"
              className="max-h-[80vh] w-auto object-contain rounded-2xl border border-[#D4AF37]/30 shadow-2xl"
            />
            <div className="pt-4 flex items-center space-x-4 text-xs text-[#EBEBEB]/80">
              <span>Royal Studio Master Grade 4K</span>
              <span>•</span>
              <a
                href={selectedImage}
                download
                className="text-[#D4AF37] hover:underline flex items-center space-x-1"
              >
                <Download className="w-3.5 h-3.5" />
                <span>Save to Device</span>
              </a>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
