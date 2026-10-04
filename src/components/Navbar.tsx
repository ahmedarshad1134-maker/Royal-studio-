import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import {
  Camera,
  Calendar,
  Image,
  Award,
  Star,
  Phone,
  Info,
  User,
  Shield,
  Menu,
  X,
  Bell,
  LogOut,
  ChevronRight,
  Package,
} from 'lucide-react';
import { ScreenId } from '../types';

interface NavbarProps {
  onOpenNotifications: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({ onOpenNotifications }) => {
  const { currentScreen, navigateTo, currentUser, logout, notifications, settings } = useApp();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const unreadCount = notifications.filter((n) => {
    if (currentUser?.role === 'admin') return n.isAdminAlert && !n.read;
    if (currentUser?.role === 'customer') return n.recipientUserId === currentUser.uid && !n.read;
    return false;
  }).length;

  const navLinks: { id: ScreenId; label: string; icon: React.ElementType }[] = [
    { id: 'home', label: 'Home', icon: Camera },
    { id: 'portfolio', label: 'Portfolio', icon: Image },
    { id: 'services', label: 'Services', icon: Award },
    { id: 'packages', label: 'Packages', icon: Package },
    { id: 'booking', label: 'Book Now', icon: Calendar },
    { id: 'reviews', label: 'Reviews', icon: Star },
    { id: 'about', label: 'About', icon: Info },
    { id: 'contact', label: 'Contact', icon: Phone },
  ];

  const handleNavClick = (screen: ScreenId) => {
    navigateTo(screen);
    setMobileMenuOpen(false);
  };

  return (
    <header className="sticky top-0 z-40 bg-[#121212]/90 backdrop-blur-md border-b border-[#D4AF37]/20">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-20">
          {/* Logo & Brand */}
          <div
            onClick={() => handleNavClick('home')}
            className="flex items-center space-x-3 cursor-pointer group"
          >
            <div className="w-11 h-11 rounded-full border-2 border-[#D4AF37] p-0.5 shadow-md shadow-[#D4AF37]/20 transition-transform group-hover:scale-105">
              <img
                src="/assets/images/app_icon_foreground_1789656291104.jpg"
                alt="Royal Studio"
                className="w-full h-full object-cover rounded-full"
                onError={(e) => {
                  (e.target as HTMLElement).style.display = 'none';
                }}
              />
            </div>
            <div>
              <span className="font-serif text-2xl font-bold tracking-wider text-[#D4AF37] block leading-none">
                {settings.studioName || 'ROYAL STUDIO'}
              </span>
              <span className="text-[10px] tracking-[0.25em] text-[#EBEBEB]/60 uppercase font-sans">
                Fine-Art Cinema & Photo
              </span>
            </div>
          </div>

          {/* Desktop Nav Links */}
          <nav className="hidden lg:flex items-center space-x-1">
            {navLinks.map((link) => {
              const isActive = currentScreen === link.id;
              return (
                <button
                  key={link.id}
                  onClick={() => handleNavClick(link.id)}
                  className={`px-3 py-2 text-sm font-medium transition-all rounded-md flex items-center space-x-1.5 ${
                    isActive
                      ? 'text-[#D4AF37] bg-[#D4AF37]/10 border-b-2 border-[#D4AF37]'
                      : 'text-[#EBEBEB]/80 hover:text-[#D4AF37] hover:bg-white/5'
                  }`}
                >
                  <link.icon className="w-4 h-4" />
                  <span>{link.label}</span>
                </button>
              );
            })}
          </nav>

          {/* Actions: Notifications, Client Area, Admin */}
          <div className="hidden md:flex items-center space-x-3">
            {/* Notification Bell */}
            <button
              onClick={onOpenNotifications}
              className="relative p-2 rounded-full text-[#EBEBEB]/80 hover:text-[#D4AF37] hover:bg-white/5 transition"
              title="Notifications"
            >
              <Bell className="w-5 h-5" />
              {unreadCount > 0 && (
                <span className="absolute top-1 right-1 flex items-center justify-center min-w-4 h-4 px-1 text-[10px] font-bold text-white bg-red-600 rounded-full">
                  {unreadCount}
                </span>
              )}
            </button>

            {/* Client Area button */}
            <button
              onClick={() => handleNavClick('customer_area')}
              className={`px-3.5 py-1.5 rounded-full text-xs font-semibold tracking-wide transition flex items-center space-x-1.5 border ${
                currentScreen === 'customer_area'
                  ? 'bg-[#D4AF37] text-black border-[#D4AF37]'
                  : 'text-[#D4AF37] border-[#D4AF37]/50 hover:bg-[#D4AF37]/15'
              }`}
            >
              <User className="w-3.5 h-3.5" />
              <span>
                {currentUser?.role === 'customer' ? currentUser.name.split(' ')[0] : 'Client Area'}
              </span>
            </button>

            {/* Admin Portal Button */}
            <button
              onClick={() => handleNavClick('admin')}
              className={`p-2 rounded-full transition border ${
                currentScreen === 'admin'
                  ? 'bg-red-500/20 text-red-400 border-red-500/40'
                  : 'text-[#EBEBEB]/60 border-transparent hover:text-red-400 hover:bg-red-500/10'
              }`}
              title="Admin Portal"
            >
              <Shield className="w-4 h-4" />
            </button>

            {/* User Logout (if authenticated) */}
            {currentUser && (
              <button
                onClick={logout}
                className="p-2 text-[#EBEBEB]/50 hover:text-red-400 rounded-full hover:bg-white/5 transition"
                title="Sign Out"
              >
                <LogOut className="w-4 h-4" />
              </button>
            )}
          </div>

          {/* Mobile hamburger */}
          <div className="flex items-center space-x-2 lg:hidden">
            <button
              onClick={onOpenNotifications}
              className="relative p-2 text-[#EBEBEB]/80 hover:text-[#D4AF37]"
            >
              <Bell className="w-5 h-5" />
              {unreadCount > 0 && (
                <span className="absolute top-1 right-1 w-2.5 h-2.5 bg-red-600 rounded-full" />
              )}
            </button>
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="p-2 text-[#D4AF37] hover:bg-white/5 rounded-md"
            >
              {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile Menu Dropdown */}
      {mobileMenuOpen && (
        <div className="lg:hidden bg-[#18181A] border-b border-[#D4AF37]/20 px-4 pt-2 pb-6 space-y-2 animate-fadeIn">
          {navLinks.map((link) => (
            <button
              key={link.id}
              onClick={() => handleNavClick(link.id)}
              className={`w-full flex items-center justify-between px-3 py-2.5 rounded-lg text-sm font-medium ${
                currentScreen === link.id
                  ? 'bg-[#D4AF37]/15 text-[#D4AF37] font-semibold'
                  : 'text-[#EBEBEB]/80 hover:bg-white/5'
              }`}
            >
              <div className="flex items-center space-x-3">
                <link.icon className="w-4 h-4 text-[#D4AF37]" />
                <span>{link.label}</span>
              </div>
              <ChevronRight className="w-4 h-4 text-[#EBEBEB]/40" />
            </button>
          ))}

          <div className="pt-4 border-t border-white/10 space-y-2">
            <button
              onClick={() => handleNavClick('customer_area')}
              className="w-full flex items-center justify-center space-x-2 py-2.5 rounded-lg bg-[#D4AF37] text-black font-semibold text-sm"
            >
              <User className="w-4 h-4" />
              <span>{currentUser?.role === 'customer' ? `Account (${currentUser.name})` : 'Client Area Sign In'}</span>
            </button>
            <button
              onClick={() => handleNavClick('admin')}
              className="w-full flex items-center justify-center space-x-2 py-2.5 rounded-lg border border-red-500/30 text-red-400 hover:bg-red-500/10 text-sm font-medium"
            >
              <Shield className="w-4 h-4" />
              <span>Admin Management Portal</span>
            </button>
            {currentUser && (
              <button
                onClick={() => {
                  logout();
                  setMobileMenuOpen(false);
                }}
                className="w-full flex items-center justify-center space-x-2 py-2 rounded-lg text-[#EBEBEB]/60 hover:text-red-400 text-xs"
              >
                <LogOut className="w-3.5 h-3.5" />
                <span>Sign Out ({currentUser.email})</span>
              </button>
            )}
          </div>
        </div>
      )}
    </header>
  );
};
