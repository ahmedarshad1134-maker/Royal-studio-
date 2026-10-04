import React from 'react';
import { useApp } from '../context/AppContext';
import { Phone, Mail, MapPin, Clock, MessageSquare, Instagram, Facebook, Youtube } from 'lucide-react';
import { ScreenId } from '../types';

export const Footer: React.FC = () => {
  const { settings, navigateTo } = useApp();

  return (
    <footer className="bg-[#0D0D0E] border-t border-[#D4AF37]/20 pt-16 pb-12 text-[#EBEBEB]">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-10 mb-12">
          {/* Column 1: Studio Story */}
          <div className="space-y-4">
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 rounded-full border border-[#D4AF37] p-0.5">
                <img
                  src="/assets/images/app_icon_foreground_1789656291104.jpg"
                  alt="Royal Studio"
                  className="w-full h-full object-cover rounded-full"
                />
              </div>
              <span className="font-serif text-2xl font-bold tracking-wider text-[#D4AF37]">
                {settings.studioName}
              </span>
            </div>
            <p className="text-sm text-[#EBEBEB]/70 leading-relaxed font-sans">
              Capturing extraordinary human love, grand weddings, and royal milestones with timeless cinematic mastery.
            </p>
            <div className="flex items-center space-x-4 pt-2">
              <a
                href={settings.socialLinks.Instagram || 'https://instagram.com'}
                target="_blank"
                rel="noreferrer"
                className="w-9 h-9 rounded-full bg-white/5 flex items-center justify-center text-[#D4AF37] hover:bg-[#D4AF37] hover:text-black transition"
                title="Instagram"
              >
                <Instagram className="w-4 h-4" />
              </a>
              <a
                href={settings.socialLinks.Facebook || 'https://facebook.com'}
                target="_blank"
                rel="noreferrer"
                className="w-9 h-9 rounded-full bg-white/5 flex items-center justify-center text-[#D4AF37] hover:bg-[#D4AF37] hover:text-black transition"
                title="Facebook"
              >
                <Facebook className="w-4 h-4" />
              </a>
              <a
                href={settings.socialLinks.YouTube || 'https://youtube.com'}
                target="_blank"
                rel="noreferrer"
                className="w-9 h-9 rounded-full bg-white/5 flex items-center justify-center text-[#D4AF37] hover:bg-[#D4AF37] hover:text-black transition"
                title="YouTube"
              >
                <Youtube className="w-4 h-4" />
              </a>
            </div>
          </div>

          {/* Column 2: Quick Links */}
          <div>
            <h4 className="font-serif text-lg font-bold text-[#D4AF37] mb-4 tracking-wide">
              Explore
            </h4>
            <ul className="space-y-2.5 text-sm text-[#EBEBEB]/80 font-medium">
              {[
                { label: 'Wedding Cinematography', screen: 'services' as ScreenId },
                { label: 'Curated Portfolio', screen: 'portfolio' as ScreenId },
                { label: 'Signature Packages', screen: 'packages' as ScreenId },
                { label: 'Book an Enquiry', screen: 'booking' as ScreenId },
                { label: 'Client Reviews', screen: 'reviews' as ScreenId },
                { label: 'Our Story & Philosophy', screen: 'about' as ScreenId },
              ].map((item, idx) => (
                <li key={idx}>
                  <button
                    onClick={() => navigateTo(item.screen)}
                    className="hover:text-[#D4AF37] transition text-left"
                  >
                    {item.label}
                  </button>
                </li>
              ))}
            </ul>
          </div>

          {/* Column 3: Contact & Studio Location */}
          <div>
            <h4 className="font-serif text-lg font-bold text-[#D4AF37] mb-4 tracking-wide">
              Studio & Contact
            </h4>
            <div className="space-y-3 text-sm text-[#EBEBEB]/80">
              <div className="flex items-start space-x-3">
                <MapPin className="w-4 h-4 text-[#D4AF37] mt-0.5 shrink-0" />
                <span>{settings.address}</span>
              </div>
              <div className="flex items-center space-x-3">
                <Phone className="w-4 h-4 text-[#D4AF37] shrink-0" />
                <a href={`tel:${settings.phone}`} className="hover:text-[#D4AF37]">
                  {settings.phone}
                </a>
              </div>
              <div className="flex items-center space-x-3">
                <MessageSquare className="w-4 h-4 text-[#D4AF37] shrink-0" />
                <a
                  href={`https://wa.me/${settings.whatsapp.replace(/[^0-9]/g, '')}`}
                  target="_blank"
                  rel="noreferrer"
                  className="hover:text-[#D4AF37] flex items-center space-x-1"
                >
                  <span>WhatsApp Concierge</span>
                </a>
              </div>
              <div className="flex items-center space-x-3">
                <Mail className="w-4 h-4 text-[#D4AF37] shrink-0" />
                <a href={`mailto:${settings.email}`} className="hover:text-[#D4AF37]">
                  {settings.email}
                </a>
              </div>
            </div>
          </div>

          {/* Column 4: Hours & Client Portal */}
          <div>
            <h4 className="font-serif text-lg font-bold text-[#D4AF37] mb-4 tracking-wide">
              Studio Hours & Portal
            </h4>
            <div className="space-y-3 text-xs text-[#EBEBEB]/70 mb-5">
              <div className="flex items-start space-x-2">
                <Clock className="w-4 h-4 text-[#D4AF37] shrink-0 mt-0.5" />
                <p className="whitespace-pre-line leading-relaxed">{settings.businessHours}</p>
              </div>
            </div>
            <div className="p-3.5 rounded-xl bg-white/5 border border-[#D4AF37]/20 space-y-2">
              <p className="text-xs text-[#EBEBEB]/80 font-medium">Already have an event booked?</p>
              <button
                onClick={() => navigateTo('customer_area')}
                className="w-full py-1.5 px-3 rounded-md bg-[#D4AF37] hover:bg-[#AA8C2C] text-black font-semibold text-xs transition"
              >
                Access Client Portal & Gallery
              </button>
            </div>
          </div>
        </div>

        {/* Bottom bar */}
        <div className="pt-8 border-t border-white/10 flex flex-col sm:flex-row items-center justify-between text-xs text-[#EBEBEB]/50 gap-4">
          <p>© {new Date().getFullYear()} {settings.studioName}. All rights reserved.</p>
          <div className="flex items-center space-x-6">
            <button onClick={() => navigateTo('about')} className="hover:text-[#D4AF37]">About</button>
            <button onClick={() => navigateTo('contact')} className="hover:text-[#D4AF37]">Contact</button>
            <button onClick={() => navigateTo('customer_area')} className="hover:text-[#D4AF37]">Client Area</button>
            <button onClick={() => navigateTo('admin')} className="hover:text-red-400">Admin</button>
          </div>
        </div>
      </div>
    </footer>
  );
};
