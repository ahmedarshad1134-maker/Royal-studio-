import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import {
  User as UserIcon,
  Shield,
  Calendar,
  MapPin,
  Package as PackageIcon,
  CheckCircle2,
  Clock,
  Download,
  ExternalLink,
  LogOut,
  Mail,
  Lock,
  MessageSquare,
  FileText,
  AlertCircle,
  Sparkles,
  DollarSign,
  ChevronRight,
  ArrowLeft,
} from 'lucide-react';
import { BookingStatus, Booking } from '../types';

export const CustomerAreaScreen: React.FC = () => {
  const {
    currentUser,
    login,
    logout,
    bookings,
    invoices,
    packages,
    settings,
    navigateTo,
    demoUsers,
  } = useApp();

  // Auth form state
  const [isSignUp, setIsSignUp] = useState(false);
  const [authName, setAuthName] = useState('');
  const [authEmail, setAuthEmail] = useState('');
  const [authPhone, setAuthPhone] = useState('');
  const [authPassword, setAuthPassword] = useState('');
  const [authError, setAuthError] = useState('');
  const [showForgotPassword, setShowForgotPassword] = useState(false);
  const [resetSent, setResetSent] = useState(false);

  // Selected booking in dashboard
  const userBookings = bookings.filter((b) => {
    if (!currentUser) return false;
    return (
      b.customerId === currentUser.uid ||
      b.email.toLowerCase() === currentUser.email.toLowerCase()
    );
  });

  const [selectedBookingId, setSelectedBookingId] = useState<string>(() => {
    return userBookings[0]?.id || '';
  });

  const activeBooking = userBookings.find((b) => b.id === selectedBookingId) || userBookings[0];
  const activeInvoice = activeBooking ? invoices.find((i) => i.bookingId === activeBooking.id) : null;

  // Sign in / Sign up submit
  const handleAuthSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setAuthError('');

    if (!authEmail || !authEmail.includes('@')) {
      setAuthError('Please enter a valid email address.');
      return;
    }
    if (isSignUp && !authName.trim()) {
      setAuthError('Please enter your full name.');
      return;
    }
    if (!authPassword || authPassword.length < 4) {
      setAuthError('Password must be at least 4 characters.');
      return;
    }

    try {
      await login(authEmail.trim(), 'customer', authName.trim() || undefined, authPhone.trim() || undefined);
    } catch {
      setAuthError('Authentication failed. Please try again.');
    }
  };

  // If user is Admin, direct to Admin panel
  if (currentUser?.role === 'admin') {
    return (
      <div className="min-h-screen bg-[#0E0E10] text-[#EBEBEB] py-20 px-4 flex items-center justify-center">
        <div className="max-w-md w-full p-8 rounded-3xl bg-[#141416] border border-[#D4AF37]/30 text-center space-y-6 shadow-2xl">
          <div className="w-16 h-16 rounded-full bg-[#D4AF37]/15 border border-[#D4AF37] flex items-center justify-center mx-auto text-[#D4AF37]">
            <Shield className="w-8 h-8" />
          </div>
          <div className="space-y-2">
            <h2 className="font-serif text-2xl font-bold text-white">Administrator Session Active</h2>
            <p className="text-xs text-[#EBEBEB]/70 leading-relaxed">
              You are currently logged in as a Studio Administrator ({currentUser.email}). Customer bookings and private deliverables are managed through the Admin Management Portal.
            </p>
          </div>
          <div className="space-y-3 pt-2">
            <button
              onClick={() => navigateTo('admin')}
              className="w-full py-3.5 rounded-xl bg-[#D4AF37] text-black font-bold text-xs uppercase tracking-wider hover:bg-[#AA8C2C] transition"
            >
              Go to Admin Management Portal
            </button>
            <button
              onClick={logout}
              className="w-full py-3 rounded-xl border border-white/10 hover:bg-white/5 text-xs text-[#EBEBEB]/80 font-medium transition"
            >
              Sign Out
            </button>
          </div>
        </div>
      </div>
    );
  }

  // If user is not authenticated: Show Sign In / Sign Up view
  if (!currentUser) {
    return (
      <div className="min-h-screen bg-[#0E0E10] text-[#EBEBEB] py-16 px-4 flex items-center justify-center">
        <div className="max-w-md w-full space-y-8">
          <div className="text-center space-y-3">
            <div className="w-14 h-14 rounded-full border-2 border-[#D4AF37] p-0.5 mx-auto">
              <img
                src="/assets/images/app_icon_foreground_1789656291104.jpg"
                alt="Royal Studio"
                className="w-full h-full object-cover rounded-full"
              />
            </div>
            <h2 className="font-serif text-3xl font-bold text-white">Client Portal</h2>
            <p className="text-xs text-[#EBEBEB]/70">
              {isSignUp
                ? 'Create a client account to track your sessions and private galleries'
                : 'Sign in to access your bookings, timelines, invoices, and private gallery'}
            </p>
          </div>

          <div className="rounded-3xl bg-[#141416] border border-white/5 p-8 shadow-2xl space-y-6">
            {/* Tabs */}
            <div className="flex p-1 rounded-xl bg-black/40 border border-white/10">
              <button
                type="button"
                onClick={() => {
                  setIsSignUp(false);
                  setAuthError('');
                }}
                className={`flex-1 py-2 text-xs font-bold rounded-lg transition ${
                  !isSignUp ? 'bg-[#D4AF37] text-black' : 'text-[#EBEBEB]/70 hover:text-white'
                }`}
              >
                Sign In
              </button>
              <button
                type="button"
                onClick={() => {
                  setIsSignUp(true);
                  setAuthError('');
                }}
                className={`flex-1 py-2 text-xs font-bold rounded-lg transition ${
                  isSignUp ? 'bg-[#D4AF37] text-black' : 'text-[#EBEBEB]/70 hover:text-white'
                }`}
              >
                Create Account
              </button>
            </div>

            {authError && (
              <div className="p-3.5 rounded-xl bg-red-500/10 border border-red-500/30 text-xs text-red-400 flex items-center space-x-2">
                <AlertCircle className="w-4 h-4 shrink-0" />
                <span>{authError}</span>
              </div>
            )}

            {resetSent && (
              <div className="p-3.5 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-xs text-emerald-400 flex items-center space-x-2">
                <CheckCircle2 className="w-4 h-4 shrink-0" />
                <span>Password reset link sent to your email.</span>
              </div>
            )}

            <form onSubmit={handleAuthSubmit} className="space-y-4">
              {isSignUp && (
                <div>
                  <label className="block text-xs font-semibold text-[#D4AF37] uppercase tracking-wider mb-1">
                    Full Name *
                  </label>
                  <div className="relative">
                    <input
                      type="text"
                      required
                      value={authName}
                      onChange={(e) => setAuthName(e.target.value)}
                      placeholder="e.g. Sarah Jenkins"
                      className="w-full px-4 py-3 pl-11 rounded-xl bg-black/40 border border-white/10 text-white text-sm outline-none focus:border-[#D4AF37]"
                    />
                    <UserIcon className="w-4 h-4 text-[#D4AF37] absolute left-4 top-1/2 -translate-y-1/2" />
                  </div>
                </div>
              )}

              <div>
                <label className="block text-xs font-semibold text-[#D4AF37] uppercase tracking-wider mb-1">
                  Email Address *
                </label>
                <div className="relative">
                  <input
                    type="email"
                    required
                    value={authEmail}
                    onChange={(e) => setAuthEmail(e.target.value)}
                    placeholder="client@example.com"
                    className="w-full px-4 py-3 pl-11 rounded-xl bg-black/40 border border-white/10 text-white text-sm outline-none focus:border-[#D4AF37]"
                  />
                  <Mail className="w-4 h-4 text-[#D4AF37] absolute left-4 top-1/2 -translate-y-1/2" />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-[#D4AF37] uppercase tracking-wider mb-1">
                  Password *
                </label>
                <div className="relative">
                  <input
                    type="password"
                    required
                    value={authPassword}
                    onChange={(e) => setAuthPassword(e.target.value)}
                    placeholder="••••••••"
                    className="w-full px-4 py-3 pl-11 rounded-xl bg-black/40 border border-white/10 text-white text-sm outline-none focus:border-[#D4AF37]"
                  />
                  <Lock className="w-4 h-4 text-[#D4AF37] absolute left-4 top-1/2 -translate-y-1/2" />
                </div>
              </div>

              {!isSignUp && (
                <div className="text-right">
                  <button
                    type="button"
                    onClick={() => setShowForgotPassword(true)}
                    className="text-xs text-[#D4AF37] hover:underline"
                  >
                    Forgot Password?
                  </button>
                </div>
              )}

              <button
                type="submit"
                className="w-full py-3.5 rounded-xl bg-[#D4AF37] hover:bg-[#AA8C2C] text-black font-bold text-xs uppercase tracking-wider transition shadow-lg shadow-[#D4AF37]/20"
              >
                {isSignUp ? 'Create Client Account' : 'Sign In'}
              </button>
            </form>

            {/* Quick Demo Login Option */}
            <div className="pt-4 border-t border-white/10 text-center space-y-2">
              <p className="text-[11px] text-[#EBEBEB]/50 uppercase tracking-wider">
                Instant Demo Access
              </p>
              <button
                type="button"
                onClick={() => {
                  login('sarah.jenkins@example.com', 'customer', 'Sarah Jenkins', '+1 555-019-2831');
                }}
                className="w-full py-2.5 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 text-xs text-[#EBEBEB] font-medium transition"
              >
                Sign In as Demo Client (Sarah Jenkins - Active Booking)
              </button>
            </div>
          </div>
        </div>

        {/* Forgot password modal */}
        {showForgotPassword && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
            <div className="max-w-sm w-full p-6 rounded-2xl bg-[#18181A] border border-[#D4AF37]/30 space-y-4">
              <h3 className="font-serif text-lg font-bold text-white">Reset Password</h3>
              <p className="text-xs text-[#EBEBEB]/70">
                Enter your email address and we will send a password reset verification link.
              </p>
              <input
                type="email"
                placeholder="youremail@example.com"
                defaultValue={authEmail}
                className="w-full px-4 py-2.5 rounded-xl bg-black/40 border border-white/10 text-sm text-white outline-none"
              />
              <div className="flex justify-end space-x-2 pt-2">
                <button
                  onClick={() => setShowForgotPassword(false)}
                  className="px-4 py-2 text-xs text-[#EBEBEB]/60 hover:text-white"
                >
                  Cancel
                </button>
                <button
                  onClick={() => {
                    setShowForgotPassword(false);
                    setResetSent(true);
                  }}
                  className="px-5 py-2 rounded-xl bg-[#D4AF37] text-black font-bold text-xs"
                >
                  Send Reset Link
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    );
  }

  // If user is authenticated as customer:
  const getStatusBadge = (status: BookingStatus) => {
    switch (status) {
      case 'NEW':
        return { text: 'Enquiry Received', color: 'border-blue-500/40 bg-blue-500/10 text-blue-400' };
      case 'CONTACTED':
        return { text: 'Contacted by Producer', color: 'border-purple-500/40 bg-purple-500/10 text-purple-400' };
      case 'QUOTATION_SENT':
        return { text: 'Quotation Issued', color: 'border-amber-500/40 bg-amber-500/10 text-amber-400' };
      case 'CONFIRMED':
        return { text: 'Confirmed & Scheduled', color: 'border-emerald-500/40 bg-emerald-500/10 text-emerald-400' };
      case 'COMPLETED':
        return { text: 'Production Completed', color: 'border-[#D4AF37]/40 bg-[#D4AF37]/10 text-[#D4AF37]' };
      case 'CANCELLED':
        return { text: 'Cancelled', color: 'border-red-500/40 bg-red-500/10 text-red-400' };
      default:
        return { text: status, color: 'border-white/20 bg-white/5 text-white' };
    }
  };

  return (
    <div className="min-h-screen bg-[#0E0E10] text-[#EBEBEB] py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-5xl mx-auto space-y-10">
        {/* Top Bar */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-6 rounded-3xl bg-[#141416] border border-white/5 shadow-xl">
          <div className="flex items-center space-x-4">
            <div className="w-12 h-12 rounded-full bg-[#D4AF37]/20 border border-[#D4AF37] flex items-center justify-center text-[#D4AF37] font-bold text-lg">
              {currentUser.name.charAt(0)}
            </div>
            <div>
              <p className="text-[11px] text-[#EBEBEB]/60 uppercase tracking-wider">Client Portal</p>
              <h2 className="font-serif text-2xl font-bold text-white">{currentUser.name}</h2>
              <p className="text-xs text-[#EBEBEB]/50">{currentUser.email}</p>
            </div>
          </div>

          <div className="flex items-center space-x-3">
            <button
              onClick={() => navigateTo('booking')}
              className="px-4 py-2 rounded-xl bg-white/5 hover:bg-white/10 text-xs font-semibold text-white border border-white/10 transition"
            >
              + Book Another Session
            </button>
            <button
              onClick={logout}
              className="px-3.5 py-2 rounded-xl text-xs font-semibold text-red-400 hover:bg-red-500/10 border border-red-500/20 transition flex items-center space-x-1.5"
            >
              <LogOut className="w-3.5 h-3.5" />
              <span>Sign Out</span>
            </button>
          </div>
        </div>

        {/* If no bookings exist */}
        {userBookings.length === 0 ? (
          <div className="py-20 text-center rounded-3xl bg-[#141416] border border-white/5 p-8 space-y-4">
            <Calendar className="w-12 h-12 mx-auto text-[#D4AF37]/60" />
            <h3 className="font-serif text-2xl font-bold text-white">No Active Bookings Found</h3>
            <p className="text-xs text-[#EBEBEB]/70 max-w-md mx-auto">
              You do not have any registered events or enquiries yet. When you submit a booking enquiry, your itinerary, invoices, and private deliverables will appear here.
            </p>
            <button
              onClick={() => navigateTo('booking')}
              className="px-8 py-3.5 rounded-xl bg-[#D4AF37] text-black font-bold text-xs uppercase tracking-wider hover:bg-[#AA8C2C] transition"
            >
              Book a Session Now
            </button>
          </div>
        ) : (
          <div className="space-y-8">
            {/* Multiple Bookings Switcher if > 1 */}
            {userBookings.length > 1 && (
              <div className="flex items-center space-x-3 overflow-x-auto pb-2">
                <span className="text-xs text-[#EBEBEB]/60 uppercase font-semibold shrink-0">
                  Select Event:
                </span>
                {userBookings.map((b) => (
                  <button
                    key={b.id}
                    onClick={() => setSelectedBookingId(b.id)}
                    className={`px-4 py-2 rounded-xl text-xs font-bold transition shrink-0 ${
                      activeBooking.id === b.id
                        ? 'bg-[#D4AF37] text-black shadow-md'
                        : 'bg-white/5 text-[#EBEBEB]/70 hover:bg-white/10'
                    }`}
                  >
                    #{b.referenceId} ({b.eventType})
                  </button>
                ))}
              </div>
            )}

            {/* Status Banner */}
            {(() => {
              const badge = getStatusBadge(activeBooking.status);
              return (
                <div
                  className={`p-5 rounded-2xl border flex items-center justify-between gap-4 ${badge.color}`}
                >
                  <div className="flex items-center space-x-3">
                    <CheckCircle2 className="w-5 h-5 shrink-0" />
                    <div>
                      <p className="text-[11px] uppercase tracking-wider font-bold">Booking Status</p>
                      <p className="font-serif text-lg font-bold">{badge.text}</p>
                    </div>
                  </div>
                  <span className="text-xs font-mono font-bold px-3 py-1 rounded-full bg-black/40 border border-current">
                    #{activeBooking.referenceId}
                  </span>
                </div>
              );
            })()}

            {/* Main Booking Details & Deliverables Grid */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
              {/* Left 2 Cols: Details & Invoices */}
              <div className="lg:col-span-2 space-y-8">
                {/* Event Details Card */}
                <div className="p-6 sm:p-8 rounded-3xl bg-[#141416] border border-white/5 space-y-6">
                  <div className="flex items-center justify-between border-b border-white/10 pb-4">
                    <h3 className="font-serif text-xl font-bold text-white">Event Itinerary</h3>
                    <span className="text-xs text-[#D4AF37] font-semibold">
                      {activeBooking.eventType}
                    </span>
                  </div>

                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 text-xs">
                    <div className="space-y-1">
                      <span className="text-[#EBEBEB]/50 uppercase tracking-wider flex items-center space-x-1.5">
                        <Calendar className="w-3.5 h-3.5 text-[#D4AF37]" />
                        <span>Date & Schedule</span>
                      </span>
                      <p className="font-bold text-white text-sm">
                        {new Date(activeBooking.eventDate).toLocaleDateString([], {
                          weekday: 'long',
                          month: 'long',
                          day: 'numeric',
                          year: 'numeric',
                        })}
                      </p>
                    </div>

                    <div className="space-y-1">
                      <span className="text-[#EBEBEB]/50 uppercase tracking-wider flex items-center space-x-1.5">
                        <MapPin className="w-3.5 h-3.5 text-[#D4AF37]" />
                        <span>Venue & Location</span>
                      </span>
                      <p className="font-bold text-white text-sm">{activeBooking.eventLocation}</p>
                    </div>

                    <div className="space-y-1">
                      <span className="text-[#EBEBEB]/50 uppercase tracking-wider flex items-center space-x-1.5">
                        <PackageIcon className="w-3.5 h-3.5 text-[#D4AF37]" />
                        <span>Package Selection</span>
                      </span>
                      <p className="font-bold text-white text-sm">
                        {packages.find((p) => p.id === activeBooking.packageId)?.name || 'Standard Classic'}
                      </p>
                    </div>

                    <div className="space-y-1">
                      <span className="text-[#EBEBEB]/50 uppercase tracking-wider flex items-center space-x-1.5">
                        <UserIcon className="w-3.5 h-3.5 text-[#D4AF37]" />
                        <span>Contact Number</span>
                      </span>
                      <p className="font-bold text-white text-sm">{activeBooking.phone}</p>
                    </div>
                  </div>

                  {activeBooking.message && (
                    <div className="p-4 rounded-2xl bg-white/[0.02] border border-white/5 space-y-1 text-xs">
                      <span className="text-[#D4AF37] font-semibold">Client Special Notes:</span>
                      <p className="text-[#EBEBEB]/80 italic">"{activeBooking.message}"</p>
                    </div>
                  )}
                </div>

                {/* Customer Invoice & Billing Card */}
                {activeInvoice && (
                  <div className="p-6 sm:p-8 rounded-3xl bg-[#141416] border border-white/5 space-y-6">
                    <div className="flex items-center justify-between border-b border-white/10 pb-4">
                      <div>
                        <span className="text-[10px] text-[#EBEBEB]/50 uppercase tracking-wider">
                          Accounting & Retainer
                        </span>
                        <h3 className="font-serif text-xl font-bold text-white">
                          Invoice #{activeInvoice.invoiceNumber}
                        </h3>
                      </div>
                      <span
                        className={`px-3 py-1 rounded-full text-xs font-bold ${
                          activeInvoice.paymentStatus === 'Paid'
                            ? 'bg-emerald-500/20 text-emerald-400'
                            : activeInvoice.paymentStatus === 'Partially Paid'
                            ? 'bg-amber-500/20 text-amber-400'
                            : 'bg-red-500/20 text-red-400'
                        }`}
                      >
                        {activeInvoice.paymentStatus}
                      </span>
                    </div>

                    <div className="grid grid-cols-3 gap-4 text-center">
                      <div className="p-4 rounded-2xl bg-white/[0.02] border border-white/5">
                        <p className="text-[10px] text-[#EBEBEB]/50 uppercase">Total Amount</p>
                        <p className="font-serif text-lg sm:text-2xl font-bold text-white">
                          ${activeInvoice.totalAmount.toLocaleString()}
                        </p>
                      </div>
                      <div className="p-4 rounded-2xl bg-white/[0.02] border border-white/5">
                        <p className="text-[10px] text-[#EBEBEB]/50 uppercase">Amount Paid</p>
                        <p className="font-serif text-lg sm:text-2xl font-bold text-emerald-400">
                          ${activeInvoice.amountPaid.toLocaleString()}
                        </p>
                      </div>
                      <div className="p-4 rounded-2xl bg-white/[0.02] border border-white/5">
                        <p className="text-[10px] text-[#EBEBEB]/50 uppercase">Balance Due</p>
                        <p className="font-serif text-lg sm:text-2xl font-bold text-[#D4AF37]">
                          ${activeInvoice.balanceDue.toLocaleString()}
                        </p>
                      </div>
                    </div>

                    {/* Line Items */}
                    <div className="space-y-2 pt-2 border-t border-white/5">
                      <p className="text-[11px] font-bold uppercase tracking-wider text-[#D4AF37]">
                        Line Items Breakdown
                      </p>
                      {activeInvoice.lineItems.map((item, idx) => (
                        <div
                          key={idx}
                          className="flex items-center justify-between text-xs py-1.5 border-b border-white/[0.03]"
                        >
                          <span className="text-[#EBEBEB]/80">{item.description}</span>
                          <span className="font-mono text-white">
                            {item.amount < 0 ? `-$${Math.abs(item.amount)}` : `$${item.amount}`}
                          </span>
                        </div>
                      ))}
                    </div>

                    {/* Payment History Records */}
                    {activeInvoice.paymentRecords.length > 0 && (
                      <div className="space-y-2 pt-2">
                        <p className="text-[11px] font-bold uppercase tracking-wider text-[#D4AF37]">
                          Recorded Receipts
                        </p>
                        {activeInvoice.paymentRecords.map((pay) => (
                          <div
                            key={pay.paymentId}
                            className="p-3 rounded-xl bg-white/[0.02] border border-white/5 flex items-center justify-between text-xs"
                          >
                            <div>
                              <p className="font-semibold text-white">
                                {pay.method} • {pay.receiptId}
                              </p>
                              <p className="text-[10px] text-[#EBEBEB]/50">{pay.referenceNotes}</p>
                            </div>
                            <span className="font-bold text-emerald-400 font-mono">
                              +${pay.amount.toLocaleString()}
                            </span>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                )}
              </div>

              {/* Right Col: Media Deliverables Gallery & Timeline */}
              <div className="space-y-8">
                {/* Deliverables Card */}
                <div className="p-6 sm:p-8 rounded-3xl bg-gradient-to-b from-[#1C1C1F] to-[#141416] border border-[#D4AF37]/40 shadow-xl space-y-6">
                  <div className="space-y-2">
                    <span className="text-[10px] text-[#D4AF37] font-bold uppercase tracking-widest">
                      Visual Deliverables
                    </span>
                    <h3 className="font-serif text-2xl font-bold text-white">
                      Private Media Gallery
                    </h3>
                    <p className="text-xs text-[#EBEBEB]/70 font-light leading-relaxed">
                      Access 4K cinematic film cuts, retouched photo galleries, and digital heirloom albums.
                    </p>
                  </div>

                  <div className="p-4 rounded-2xl bg-black/50 border border-white/5 space-y-2">
                    <div className="flex items-center space-x-2 text-xs text-[#D4AF37]">
                      <Sparkles className="w-4 h-4" />
                      <span className="font-bold">Gallery Status: Ready to View</span>
                    </div>
                    <p className="text-[11px] text-[#EBEBEB]/60 leading-relaxed">
                      High-resolution previews and master downloads are unlocked for this booking.
                    </p>
                  </div>

                  <button
                    onClick={() => navigateTo('private_gallery', { bookingId: activeBooking.id })}
                    className="w-full py-4 rounded-xl bg-[#D4AF37] hover:bg-[#AA8C2C] text-black font-bold text-xs uppercase tracking-wider transition shadow-lg shadow-[#D4AF37]/20 flex items-center justify-center space-x-2"
                  >
                    <span>Open Private Gallery</span>
                    <ExternalLink className="w-4 h-4" />
                  </button>
                </div>

                {/* Timeline History */}
                <div className="p-6 rounded-3xl bg-[#141416] border border-white/5 space-y-4">
                  <h4 className="font-serif text-lg font-bold text-white">Updates & Progress</h4>
                  <div className="space-y-4 relative before:absolute before:left-3 before:top-2 before:bottom-2 before:w-0.5 before:bg-white/10">
                    {activeBooking.auditTrail.map((entry, idx) => (
                      <div key={idx} className="relative pl-8 space-y-1">
                        <div className="absolute left-1.5 top-1 w-3 h-3 rounded-full bg-[#D4AF37] ring-4 ring-[#141416]" />
                        <div className="flex items-center justify-between text-xs">
                          <span className="font-bold text-white">{entry.status}</span>
                          <span className="text-[10px] text-[#EBEBEB]/40">
                            {new Date(entry.timestamp).toLocaleDateString([], {
                              month: 'short',
                              day: 'numeric',
                            })}
                          </span>
                        </div>
                        <p className="text-xs text-[#EBEBEB]/70">{entry.note}</p>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Need Help Card */}
                <div className="p-6 rounded-3xl bg-[#141416] border border-white/5 text-center space-y-3">
                  <h4 className="font-serif text-base font-bold text-white">Have Questions?</h4>
                  <p className="text-xs text-[#EBEBEB]/70">
                    Our lead producer is directly available to coordinate your event timeline.
                  </p>
                  <a
                    href={`https://wa.me/${settings.whatsapp.replace(/[^0-9]/g, '')}?text=${encodeURIComponent(
                      `Hello Royal Studio, I am inquiring regarding booking #${activeBooking.referenceId}.`
                    )}`}
                    target="_blank"
                    rel="noreferrer"
                    className="inline-flex items-center space-x-1.5 text-xs font-bold text-[#D4AF37] hover:underline"
                  >
                    <MessageSquare className="w-3.5 h-3.5" />
                    <span>Chat on WhatsApp ({settings.whatsapp})</span>
                  </a>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
