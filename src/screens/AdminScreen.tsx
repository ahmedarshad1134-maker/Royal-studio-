import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import {
  Shield,
  Lock,
  LogOut,
  Calendar,
  FileText,
  DollarSign,
  Image,
  Award,
  Package,
  Star,
  Settings,
  AlertTriangle,
  CheckCircle2,
  X,
  Search,
  Plus,
  Trash2,
  Edit,
  Eye,
  ArrowLeft,
  Users,
  Check,
  Ban,
  Clock,
  ExternalLink,
} from 'lucide-react';
import {
  AdminSection,
  BookingStatus,
  PortfolioCategory,
  MediaType,
  PaymentStatus,
  Booking,
  Invoice,
} from '../types';

export const AdminScreen: React.FC = () => {
  const {
    currentUser,
    login,
    logout,
    bookings,
    updateBookingStatus,
    updateBookingNotes,
    invoices,
    createInvoiceForBooking,
    recordPayment,
    portfolio,
    addPortfolioItem,
    updatePortfolioItem,
    deletePortfolioItem,
    services,
    updateService,
    addService,
    packages,
    updatePackage,
    addPackage,
    reviews,
    moderateReview,
    deleteReview,
    blockedDates,
    addBlockedDate,
    removeBlockedDate,
    settings,
    updateSettings,
    navigateTo,
  } = useApp();

  const [currentSection, setCurrentSection] = useState<AdminSection>('DASHBOARD');

  // Admin Auth Form State
  const [adminEmail, setAdminEmail] = useState('');
  const [adminPassword, setAdminPassword] = useState('');
  const [authError, setAuthError] = useState('');

  // Bookings Section State
  const [bookingStatusFilter, setBookingStatusFilter] = useState<string>('ALL');
  const [bookingSearch, setBookingSearch] = useState('');
  const [selectedBookingForEdit, setSelectedBookingForEdit] = useState<Booking | null>(null);
  const [newStatus, setNewStatus] = useState<BookingStatus>('CONTACTED');
  const [statusNote, setStatusNote] = useState('');

  // Invoices Section State
  const [invoiceStatusFilter, setInvoiceStatusFilter] = useState<string>('ALL');
  const [invoiceSearch, setInvoiceSearch] = useState('');
  const [selectedInvoice, setSelectedInvoice] = useState<Invoice | null>(null);
  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [payAmount, setPayAmount] = useState<number>(0);
  const [payMethod, setPayMethod] = useState<'Bank Transfer' | 'Cash' | 'Card' | 'UPI' | 'Cheque'>('Bank Transfer');
  const [payNotes, setPayNotes] = useState('');

  // Blocked Dates State
  const [newBlockedDate, setNewBlockedDate] = useState('');
  const [newBlockedReason, setNewBlockedReason] = useState('');

  // Settings State Form
  const [settingsForm, setSettingsForm] = useState(settings);
  const [settingsSaved, setSettingsSaved] = useState(false);

  // New Portfolio Item Form
  const [newPortTitle, setNewPortTitle] = useState('');
  const [newPortCat, setNewPortCat] = useState<PortfolioCategory>('WEDDING');
  const [newPortUrl, setNewPortUrl] = useState('');
  const [newPortType, setNewPortType] = useState<MediaType>('IMAGE');
  const [newPortFeatured, setNewPortFeatured] = useState(false);

  const handleAdminLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setAuthError('');
    if (!adminEmail.includes('@') || !adminPassword) {
      setAuthError('Please enter valid administrator credentials.');
      return;
    }
    // Allow admin login
    await login(adminEmail, 'admin', 'Royal Studio Administrator');
  };

  // If not logged in as Admin, show Admin Login view
  if (!currentUser || currentUser.role !== 'admin') {
    return (
      <div className="min-h-screen bg-[#0E0E10] text-[#EBEBEB] py-20 px-4 flex items-center justify-center">
        <div className="max-w-md w-full p-8 rounded-3xl bg-[#141416] border border-red-500/30 text-center space-y-6 shadow-2xl">
          <div className="w-16 h-16 rounded-full bg-red-500/10 border border-red-500/40 flex items-center justify-center mx-auto text-red-400">
            <Lock className="w-8 h-8" />
          </div>

          <div className="space-y-1">
            <h2 className="font-serif text-3xl font-bold text-white">Secure Admin Portal</h2>
            <p className="text-xs text-red-400/80 font-medium tracking-wide">
              RESTRICTED ACCESS • AUTHORIZED PERSONNEL ONLY
            </p>
          </div>

          {authError && (
            <div className="p-3.5 rounded-xl bg-red-500/15 border border-red-500/30 text-xs text-red-300">
              {authError}
            </div>
          )}

          <form onSubmit={handleAdminLogin} className="space-y-4 text-left">
            <div>
              <label className="block text-xs font-semibold text-[#EBEBEB]/80 uppercase tracking-wider mb-1">
                Admin Email
              </label>
              <input
                type="email"
                required
                value={adminEmail}
                onChange={(e) => setAdminEmail(e.target.value)}
                placeholder="admin@royalstudio.com"
                className="w-full px-4 py-3 rounded-xl bg-black/50 border border-white/10 text-white text-sm outline-none focus:border-red-500"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-[#EBEBEB]/80 uppercase tracking-wider mb-1">
                Admin Password
              </label>
              <input
                type="password"
                required
                value={adminPassword}
                onChange={(e) => setAdminPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full px-4 py-3 rounded-xl bg-black/50 border border-white/10 text-white text-sm outline-none focus:border-red-500"
              />
            </div>

            <button
              type="submit"
              className="w-full py-3.5 rounded-xl bg-red-600 hover:bg-red-500 text-white font-bold text-xs uppercase tracking-wider transition shadow-lg shadow-red-600/20"
            >
              Authenticate Admin Session
            </button>
          </form>

          {/* Quick Demo Admin Button */}
          <div className="pt-4 border-t border-white/10">
            <button
              type="button"
              onClick={() => {
                login('admin@royalstudio.com', 'admin', 'Royal Studio Administrator');
              }}
              className="w-full py-2.5 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 text-xs text-[#EBEBEB]/80 font-medium transition"
            >
              Demo Quick Access: Authenticate as Studio Admin
            </button>
          </div>
        </div>
      </div>
    );
  }

  // Metrics for Dashboard
  const newEnquiriesCount = bookings.filter((b) => b.status === 'NEW').length;
  const upcomingEventsCount = bookings.filter((b) => b.eventDate > Date.now()).length;
  const confirmedBookingsCount = bookings.filter((b) => b.status === 'CONFIRMED').length;
  const pendingReviewsCount = reviews.filter((r) => !r.approved).length;

  return (
    <div className="min-h-screen bg-[#0E0E10] text-[#EBEBEB] pb-24">
      {/* Admin Header Bar */}
      <div className="sticky top-20 z-30 bg-[#161618] border-b border-white/10 px-4 sm:px-8 py-4">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <div className="flex items-center space-x-4">
            {currentSection !== 'DASHBOARD' && (
              <button
                onClick={() => setCurrentSection('DASHBOARD')}
                className="p-2 rounded-xl bg-white/5 hover:bg-white/10 text-[#EBEBEB] transition flex items-center space-x-1 text-xs"
              >
                <ArrowLeft className="w-4 h-4" />
                <span>Dashboard</span>
              </button>
            )}
            <div className="flex items-center space-x-2">
              <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse" />
              <h2 className="font-serif text-xl sm:text-2xl font-bold text-white tracking-wide">
                {currentSection === 'DASHBOARD' ? 'Operations Dashboard' : currentSection.replace('_', ' ')}
              </h2>
            </div>
          </div>

          <div className="flex items-center space-x-3">
            <span className="hidden sm:inline text-xs text-[#EBEBEB]/60 font-mono">
              {currentUser.email}
            </span>
            <button
              onClick={logout}
              className="px-3.5 py-1.5 rounded-xl bg-red-500/20 hover:bg-red-500/30 text-red-400 border border-red-500/30 text-xs font-semibold transition flex items-center space-x-1.5"
            >
              <LogOut className="w-3.5 h-3.5" />
              <span>Exit Admin</span>
            </button>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-8 pt-8">
        {/* ================= DASHBOARD OVERVIEW ================= */}
        {currentSection === 'DASHBOARD' && (
          <div className="space-y-10 animate-fadeIn">
            {/* Stat Cards */}
            <div>
              <p className="text-xs font-semibold text-[#D4AF37] uppercase tracking-wider mb-4">
                Business Key Performance Indicators
              </p>
              <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
                <div
                  onClick={() => {
                    setBookingStatusFilter('NEW');
                    setCurrentSection('BOOKINGS');
                  }}
                  className="p-6 rounded-2xl bg-[#141416] border border-white/5 hover:border-blue-500/40 cursor-pointer transition space-y-2"
                >
                  <div className="flex items-center justify-between text-blue-400">
                    <span className="text-xs font-medium">New Enquiries</span>
                    <Calendar className="w-5 h-5" />
                  </div>
                  <p className="font-serif text-4xl font-bold text-white">{newEnquiriesCount}</p>
                  <p className="text-[10px] text-[#EBEBEB]/50">Awaiting consultation</p>
                </div>

                <div
                  onClick={() => setCurrentSection('BOOKINGS')}
                  className="p-6 rounded-2xl bg-[#141416] border border-white/5 hover:border-purple-500/40 cursor-pointer transition space-y-2"
                >
                  <div className="flex items-center justify-between text-purple-400">
                    <span className="text-xs font-medium">Upcoming Events</span>
                    <Clock className="w-5 h-5" />
                  </div>
                  <p className="font-serif text-4xl font-bold text-white">{upcomingEventsCount}</p>
                  <p className="text-[10px] text-[#EBEBEB]/50">Scheduled on calendar</p>
                </div>

                <div
                  onClick={() => {
                    setBookingStatusFilter('CONFIRMED');
                    setCurrentSection('BOOKINGS');
                  }}
                  className="p-6 rounded-2xl bg-[#141416] border border-white/5 hover:border-emerald-500/40 cursor-pointer transition space-y-2"
                >
                  <div className="flex items-center justify-between text-emerald-400">
                    <span className="text-xs font-medium">Confirmed Bookings</span>
                    <CheckCircle2 className="w-5 h-5" />
                  </div>
                  <p className="font-serif text-4xl font-bold text-white">{confirmedBookingsCount}</p>
                  <p className="text-[10px] text-[#EBEBEB]/50">Retainers paid & locked</p>
                </div>

                <div
                  onClick={() => setCurrentSection('REVIEWS')}
                  className="p-6 rounded-2xl bg-[#141416] border border-white/5 hover:border-amber-500/40 cursor-pointer transition space-y-2"
                >
                  <div className="flex items-center justify-between text-amber-400">
                    <span className="text-xs font-medium">Pending Reviews</span>
                    <Star className="w-5 h-5" />
                  </div>
                  <p className="font-serif text-4xl font-bold text-white">{pendingReviewsCount}</p>
                  <p className="text-[10px] text-[#EBEBEB]/50">In moderation queue</p>
                </div>
              </div>
            </div>

            {/* Management Modules */}
            <div>
              <p className="text-xs font-semibold text-[#D4AF37] uppercase tracking-wider mb-4">
                Studio Management Modules
              </p>
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                {[
                  {
                    id: 'BOOKINGS' as AdminSection,
                    title: 'Bookings & CRM',
                    desc: 'Manage customer enquiries, update statuses, add notes, and view timelines.',
                    icon: Calendar,
                    badge: `${bookings.length} Total`,
                  },
                  {
                    id: 'INVOICES' as AdminSection,
                    title: 'Invoices & Billing',
                    desc: 'Track line items, tax, balances, and record payments with instant receipts.',
                    icon: DollarSign,
                    badge: `${invoices.length} Issued`,
                  },
                  {
                    id: 'PORTFOLIO' as AdminSection,
                    title: 'Portfolio Media',
                    desc: 'Add, toggle, categorize, and feature photographs and cinematic highlight reels.',
                    icon: Image,
                    badge: `${portfolio.length} Items`,
                  },
                  {
                    id: 'SERVICES' as AdminSection,
                    title: 'Studio Services',
                    desc: 'Manage service titles, starting rates, detailed descriptions, and deliverables.',
                    icon: Award,
                    badge: `${services.length} Services`,
                  },
                  {
                    id: 'PACKAGES' as AdminSection,
                    title: 'Packages & Pricing',
                    desc: 'Configure package inclusions, hours, crew count, album specs, and add-ons.',
                    icon: Package,
                    badge: `${packages.length} Packages`,
                  },
                  {
                    id: 'REVIEWS' as AdminSection,
                    title: 'Review Moderation',
                    desc: 'Approve or reject customer reviews and feature top love stories.',
                    icon: Star,
                    badge: `${reviews.length} Reviews`,
                  },
                  {
                    id: 'BLOCKED_DATES' as AdminSection,
                    title: 'Blocked Dates',
                    desc: 'Block specific calendar dates so customers cannot select them in the wizard.',
                    icon: Ban,
                    badge: `${blockedDates.length} Blocked`,
                  },
                  {
                    id: 'SETTINGS' as AdminSection,
                    title: 'Studio Settings',
                    desc: 'Configure studio name, phone, WhatsApp concierge, address, hours, and about text.',
                    icon: Settings,
                    badge: 'Main Config',
                  },
                ].map((mod) => (
                  <div
                    key={mod.id}
                    onClick={() => setCurrentSection(mod.id)}
                    className="p-6 rounded-2xl bg-[#141416] border border-white/5 hover:border-[#D4AF37]/50 cursor-pointer transition-all duration-200 hover:-translate-y-1 space-y-4 group"
                  >
                    <div className="flex items-center justify-between">
                      <div className="p-3 rounded-xl bg-[#D4AF37]/10 text-[#D4AF37] group-hover:bg-[#D4AF37] group-hover:text-black transition">
                        <mod.icon className="w-5 h-5" />
                      </div>
                      <span className="text-[10px] font-mono text-[#D4AF37] bg-[#D4AF37]/10 px-2 py-0.5 rounded-full">
                        {mod.badge}
                      </span>
                    </div>
                    <div>
                      <h4 className="font-serif text-lg font-bold text-white group-hover:text-[#D4AF37] transition">
                        {mod.title}
                      </h4>
                      <p className="text-xs text-[#EBEBEB]/60 mt-1 line-clamp-2 leading-relaxed">
                        {mod.desc}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* ================= BOOKINGS SECTION ================= */}
        {currentSection === 'BOOKINGS' && (
          <div className="space-y-6 animate-fadeIn">
            {/* Filter and Search Bar */}
            <div className="flex flex-col md:flex-row gap-4 justify-between items-stretch md:items-center">
              <div className="flex items-center space-x-2 overflow-x-auto pb-2 scrollbar-none">
                {['ALL', 'NEW', 'CONTACTED', 'QUOTATION_SENT', 'CONFIRMED', 'COMPLETED', 'CANCELLED'].map(
                  (st) => (
                    <button
                      key={st}
                      onClick={() => setBookingStatusFilter(st)}
                      className={`px-3 py-1.5 rounded-full text-xs font-bold transition shrink-0 ${
                        bookingStatusFilter === st
                          ? 'bg-[#D4AF37] text-black'
                          : 'bg-white/5 text-[#EBEBEB]/70 hover:bg-white/10'
                      }`}
                    >
                      {st}
                    </button>
                  )
                )}
              </div>

              <div className="relative min-w-[260px]">
                <input
                  type="text"
                  placeholder="Search customer, ref, or venue..."
                  value={bookingSearch}
                  onChange={(e) => setBookingSearch(e.target.value)}
                  className="w-full px-4 py-2 pl-9 rounded-xl bg-black/40 border border-white/10 text-xs text-white outline-none focus:border-[#D4AF37]"
                />
                <Search className="w-4 h-4 text-[#EBEBEB]/50 absolute left-3 top-1/2 -translate-y-1/2" />
              </div>
            </div>

            {/* Bookings List */}
            <div className="rounded-2xl bg-[#141416] border border-white/5 overflow-hidden shadow-xl">
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs">
                  <thead className="bg-black/40 text-[#D4AF37] uppercase tracking-wider font-semibold border-b border-white/5">
                    <tr>
                      <th className="p-4">Reference</th>
                      <th className="p-4">Customer</th>
                      <th className="p-4">Event Type</th>
                      <th className="p-4">Date</th>
                      <th className="p-4">Status</th>
                      <th className="p-4 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-white/[0.03]">
                    {bookings
                      .filter((b) =>
                        bookingStatusFilter === 'ALL' ? true : b.status === bookingStatusFilter
                      )
                      .filter((b) => {
                        if (!bookingSearch.trim()) return true;
                        const q = bookingSearch.toLowerCase();
                        return (
                          b.customerName.toLowerCase().includes(q) ||
                          b.referenceId.toLowerCase().includes(q) ||
                          b.eventLocation.toLowerCase().includes(q) ||
                          b.email.toLowerCase().includes(q)
                        );
                      })
                      .map((b) => (
                        <tr key={b.id} className="hover:bg-white/[0.02]">
                          <td className="p-4 font-mono font-bold text-[#D4AF37]">#{b.referenceId}</td>
                          <td className="p-4">
                            <p className="font-bold text-white">{b.customerName}</p>
                            <p className="text-[11px] text-[#EBEBEB]/50">{b.phone}</p>
                          </td>
                          <td className="p-4 text-[#EBEBEB]/90">{b.eventType}</td>
                          <td className="p-4">
                            {new Date(b.eventDate).toLocaleDateString([], {
                              month: 'short',
                              day: 'numeric',
                              year: 'numeric',
                            })}
                          </td>
                          <td className="p-4">
                            <span
                              className={`px-2.5 py-1 rounded-full text-[10px] font-bold ${
                                b.status === 'CONFIRMED'
                                  ? 'bg-emerald-500/20 text-emerald-400'
                                  : b.status === 'NEW'
                                  ? 'bg-blue-500/20 text-blue-400'
                                  : b.status === 'CANCELLED'
                                  ? 'bg-red-500/20 text-red-400'
                                  : 'bg-amber-500/20 text-amber-400'
                              }`}
                            >
                              {b.status}
                            </span>
                          </td>
                          <td className="p-4 text-right space-x-2">
                            <button
                              onClick={() => {
                                setSelectedBookingForEdit(b);
                                setNewStatus(b.status);
                                setStatusNote('');
                              }}
                              className="px-3 py-1.5 rounded-lg bg-white/5 hover:bg-[#D4AF37] hover:text-black text-xs font-semibold transition"
                            >
                              Update Status
                            </button>
                            {!invoices.some((inv) => inv.bookingId === b.id) && (
                              <button
                                onClick={() => {
                                  createInvoiceForBooking(b);
                                  alert(`Generated Invoice for #${b.referenceId}!`);
                                }}
                                className="px-3 py-1.5 rounded-lg bg-[#D4AF37]/20 hover:bg-[#D4AF37] hover:text-black text-[#D4AF37] text-xs font-semibold transition"
                              >
                                Create Invoice
                              </button>
                            )}
                          </td>
                        </tr>
                      ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {/* Update Status Modal */}
        {selectedBookingForEdit && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/85 backdrop-blur-sm animate-fadeIn">
            <div className="max-w-md w-full p-6 rounded-3xl bg-[#18181A] border border-[#D4AF37]/30 space-y-4">
              <div className="flex items-center justify-between pb-2 border-b border-white/10">
                <h3 className="font-serif text-lg font-bold text-white">
                  Update Booking #{selectedBookingForEdit.referenceId}
                </h3>
                <button
                  onClick={() => setSelectedBookingForEdit(null)}
                  className="p-1 text-[#EBEBEB]/60 hover:text-white"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              <div className="space-y-3 text-xs">
                <div>
                  <label className="block text-[#D4AF37] font-semibold mb-1 uppercase tracking-wider">
                    New Status
                  </label>
                  <select
                    value={newStatus}
                    onChange={(e) => setNewStatus(e.target.value as BookingStatus)}
                    className="w-full px-4 py-2.5 rounded-xl bg-black/40 border border-white/10 text-white outline-none focus:border-[#D4AF37]"
                  >
                    <option value="NEW" className="bg-[#18181B]">NEW (Enquiry Received)</option>
                    <option value="CONTACTED" className="bg-[#18181B]">CONTACTED (Consultation done)</option>
                    <option value="QUOTATION_SENT" className="bg-[#18181B]">QUOTATION_SENT (Proposal sent)</option>
                    <option value="CONFIRMED" className="bg-[#18181B]">CONFIRMED (Retainer booked)</option>
                    <option value="COMPLETED" className="bg-[#18181B]">COMPLETED (Media delivered)</option>
                    <option value="CANCELLED" className="bg-[#18181B]">CANCELLED</option>
                  </select>
                </div>

                <div>
                  <label className="block text-[#D4AF37] font-semibold mb-1 uppercase tracking-wider">
                    Audit Note / Client Notification Note
                  </label>
                  <textarea
                    rows={3}
                    value={statusNote}
                    onChange={(e) => setStatusNote(e.target.value)}
                    placeholder="e.g. Discussed venue timeline with bride. Retainer deposit verified."
                    className="w-full px-4 py-2.5 rounded-xl bg-black/40 border border-white/10 text-white outline-none focus:border-[#D4AF37]"
                  />
                </div>
              </div>

              <div className="flex justify-end space-x-2 pt-2">
                <button
                  onClick={() => setSelectedBookingForEdit(null)}
                  className="px-4 py-2 text-xs text-[#EBEBEB]/60 hover:text-white"
                >
                  Cancel
                </button>
                <button
                  onClick={() => {
                    updateBookingStatus(selectedBookingForEdit.id, newStatus, statusNote);
                    setSelectedBookingForEdit(null);
                  }}
                  className="px-5 py-2 rounded-xl bg-[#D4AF37] text-black font-bold text-xs"
                >
                  Save Status
                </button>
              </div>
            </div>
          </div>
        )}

        {/* ================= INVOICES SECTION ================= */}
        {currentSection === 'INVOICES' && (
          <div className="space-y-6 animate-fadeIn">
            <div className="flex flex-col md:flex-row gap-4 justify-between items-stretch md:items-center">
              <div className="flex items-center space-x-2 overflow-x-auto pb-2 scrollbar-none">
                {['ALL', 'Unpaid', 'Partially Paid', 'Paid', 'Refunded'].map((st) => (
                  <button
                    key={st}
                    onClick={() => setInvoiceStatusFilter(st)}
                    className={`px-3 py-1.5 rounded-full text-xs font-bold transition shrink-0 ${
                      invoiceStatusFilter === st
                        ? 'bg-[#D4AF37] text-black'
                        : 'bg-white/5 text-[#EBEBEB]/70 hover:bg-white/10'
                    }`}
                  >
                    {st}
                  </button>
                ))}
              </div>

              <div className="relative min-w-[260px]">
                <input
                  type="text"
                  placeholder="Search invoice # or customer..."
                  value={invoiceSearch}
                  onChange={(e) => setInvoiceSearch(e.target.value)}
                  className="w-full px-4 py-2 pl-9 rounded-xl bg-black/40 border border-white/10 text-xs text-white outline-none focus:border-[#D4AF37]"
                />
                <Search className="w-4 h-4 text-[#EBEBEB]/50 absolute left-3 top-1/2 -translate-y-1/2" />
              </div>
            </div>

            <div className="rounded-2xl bg-[#141416] border border-white/5 overflow-hidden shadow-xl">
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs">
                  <thead className="bg-black/40 text-[#D4AF37] uppercase tracking-wider font-semibold border-b border-white/5">
                    <tr>
                      <th className="p-4">Invoice #</th>
                      <th className="p-4">Customer</th>
                      <th className="p-4">Total Amount</th>
                      <th className="p-4">Paid</th>
                      <th className="p-4">Balance Due</th>
                      <th className="p-4">Payment Status</th>
                      <th className="p-4 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-white/[0.03]">
                    {invoices
                      .filter((i) =>
                        invoiceStatusFilter === 'ALL'
                          ? true
                          : i.paymentStatus.toLowerCase() === invoiceStatusFilter.toLowerCase()
                      )
                      .filter((i) => {
                        if (!invoiceSearch.trim()) return true;
                        const q = invoiceSearch.toLowerCase();
                        return (
                          i.invoiceNumber.toLowerCase().includes(q) ||
                          i.customerName.toLowerCase().includes(q) ||
                          i.bookingReference.toLowerCase().includes(q)
                        );
                      })
                      .map((inv) => (
                        <tr key={inv.id} className="hover:bg-white/[0.02]">
                          <td className="p-4 font-mono font-bold text-[#D4AF37]">{inv.invoiceNumber}</td>
                          <td className="p-4">
                            <p className="font-bold text-white">{inv.customerName}</p>
                            <p className="text-[10px] text-[#EBEBEB]/50">Ref #{inv.bookingReference}</p>
                          </td>
                          <td className="p-4 font-mono text-white font-bold">
                            ${inv.totalAmount.toLocaleString()}
                          </td>
                          <td className="p-4 font-mono text-emerald-400 font-bold">
                            ${inv.amountPaid.toLocaleString()}
                          </td>
                          <td className="p-4 font-mono text-[#D4AF37] font-bold">
                            ${inv.balanceDue.toLocaleString()}
                          </td>
                          <td className="p-4">
                            <span
                              className={`px-2.5 py-1 rounded-full text-[10px] font-bold ${
                                inv.paymentStatus === 'Paid'
                                  ? 'bg-emerald-500/20 text-emerald-400'
                                  : inv.paymentStatus === 'Partially Paid'
                                  ? 'bg-amber-500/20 text-amber-400'
                                  : 'bg-red-500/20 text-red-400'
                              }`}
                            >
                              {inv.paymentStatus}
                            </span>
                          </td>
                          <td className="p-4 text-right space-x-2">
                            <button
                              onClick={() => setSelectedInvoice(inv)}
                              className="px-3 py-1.5 rounded-lg bg-white/5 hover:bg-white/10 text-xs font-semibold text-white transition"
                            >
                              View / Print
                            </button>
                            {inv.balanceDue > 0 && (
                              <button
                                onClick={() => {
                                  setSelectedInvoice(inv);
                                  setPayAmount(inv.balanceDue);
                                  setShowPaymentModal(true);
                                }}
                                className="px-3 py-1.5 rounded-lg bg-emerald-600/20 hover:bg-emerald-600 text-emerald-300 hover:text-white text-xs font-semibold transition"
                              >
                                Record Payment
                              </button>
                            )}
                          </td>
                        </tr>
                      ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {/* Record Payment Modal */}
        {showPaymentModal && selectedInvoice && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/85 backdrop-blur-sm animate-fadeIn">
            <div className="max-w-md w-full p-6 rounded-3xl bg-[#18181A] border border-[#D4AF37]/30 space-y-4">
              <div className="flex items-center justify-between pb-2 border-b border-white/10">
                <h3 className="font-serif text-lg font-bold text-white">
                  Record Payment for {selectedInvoice.invoiceNumber}
                </h3>
                <button
                  onClick={() => setShowPaymentModal(false)}
                  className="p-1 text-[#EBEBEB]/60 hover:text-white"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              <div className="space-y-3 text-xs">
                <div>
                  <label className="block text-[#D4AF37] font-semibold mb-1 uppercase tracking-wider">
                    Payment Amount ($)
                  </label>
                  <input
                    type="number"
                    value={payAmount}
                    max={selectedInvoice.balanceDue}
                    onChange={(e) => setPayAmount(parseFloat(e.target.value) || 0)}
                    className="w-full px-4 py-2.5 rounded-xl bg-black/40 border border-white/10 text-white outline-none focus:border-[#D4AF37]"
                  />
                  <p className="text-[10px] text-[#EBEBEB]/50 mt-1">
                    Outstanding balance: ${selectedInvoice.balanceDue.toLocaleString()}
                  </p>
                </div>

                <div>
                  <label className="block text-[#D4AF37] font-semibold mb-1 uppercase tracking-wider">
                    Payment Method
                  </label>
                  <select
                    value={payMethod}
                    onChange={(e) => setPayMethod(e.target.value as any)}
                    className="w-full px-4 py-2.5 rounded-xl bg-black/40 border border-white/10 text-white outline-none focus:border-[#D4AF37]"
                  >
                    <option value="Bank Transfer" className="bg-[#18181B]">Bank Transfer</option>
                    <option value="Cash" className="bg-[#18181B]">Cash</option>
                    <option value="Card" className="bg-[#18181B]">Credit / Debit Card</option>
                    <option value="UPI" className="bg-[#18181B]">UPI Instant</option>
                    <option value="Cheque" className="bg-[#18181B]">Cheque</option>
                  </select>
                </div>

                <div>
                  <label className="block text-[#D4AF37] font-semibold mb-1 uppercase tracking-wider">
                    Transaction / Receipt Notes
                  </label>
                  <input
                    type="text"
                    value={payNotes}
                    onChange={(e) => setPayNotes(e.target.value)}
                    placeholder="e.g. Wire transfer wire confirmation #TXN-9021"
                    className="w-full px-4 py-2.5 rounded-xl bg-black/40 border border-white/10 text-white outline-none focus:border-[#D4AF37]"
                  />
                </div>
              </div>

              <div className="flex justify-end space-x-2 pt-2">
                <button
                  onClick={() => setShowPaymentModal(false)}
                  className="px-4 py-2 text-xs text-[#EBEBEB]/60 hover:text-white"
                >
                  Cancel
                </button>
                <button
                  onClick={() => {
                    recordPayment(selectedInvoice.id, payAmount, payMethod, payNotes || 'Payment recorded by admin');
                    setShowPaymentModal(false);
                    alert(`Payment of $${payAmount} recorded! Receipt generated.`);
                  }}
                  className="px-5 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs"
                >
                  Record & Generate Receipt
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Printable Invoice Modal */}
        {selectedInvoice && !showPaymentModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/85 backdrop-blur-sm animate-fadeIn">
            <div className="max-w-2xl w-full p-8 rounded-3xl bg-[#141416] border border-[#D4AF37]/30 space-y-6 max-h-[90vh] overflow-y-auto">
              <div className="flex items-center justify-between pb-4 border-b border-white/10">
                <div>
                  <span className="text-xs text-[#D4AF37] font-bold uppercase tracking-widest">
                    Official Invoice
                  </span>
                  <h3 className="font-serif text-2xl font-bold text-white">
                    {selectedInvoice.invoiceNumber}
                  </h3>
                </div>
                <div className="flex items-center space-x-2">
                  <button
                    onClick={() => window.print()}
                    className="px-4 py-2 rounded-xl bg-[#D4AF37] text-black font-bold text-xs uppercase"
                  >
                    Print / PDF
                  </button>
                  <button
                    onClick={() => setSelectedInvoice(null)}
                    className="p-2 text-[#EBEBEB]/60 hover:text-white"
                  >
                    <X className="w-5 h-5" />
                  </button>
                </div>
              </div>

              {/* Invoice Content */}
              <div className="space-y-6 text-xs text-[#EBEBEB]/80">
                <div className="flex justify-between">
                  <div>
                    <p className="font-serif text-lg font-bold text-white">{settings.studioName}</p>
                    <p>{settings.address}</p>
                    <p>{settings.phone}</p>
                  </div>
                  <div className="text-right">
                    <p className="font-bold text-white">Bill To:</p>
                    <p>{selectedInvoice.customerName}</p>
                    <p>{selectedInvoice.customerEmail}</p>
                    <p>{selectedInvoice.customerPhone}</p>
                  </div>
                </div>

                <div className="p-4 rounded-xl bg-white/[0.02] border border-white/5 space-y-2">
                  <p className="font-bold text-[#D4AF37]">Event Particulars</p>
                  <p>
                    {selectedInvoice.eventType} on{' '}
                    {new Date(selectedInvoice.eventDate).toLocaleDateString([], {
                      weekday: 'long',
                      month: 'long',
                      day: 'numeric',
                      year: 'numeric',
                    })}{' '}
                    at {selectedInvoice.eventLocation}
                  </p>
                </div>

                <div>
                  <table className="w-full text-left">
                    <thead className="border-b border-white/10 text-[#D4AF37]">
                      <tr>
                        <th className="py-2">Item Description</th>
                        <th className="py-2 text-right">Amount</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-white/5">
                      {selectedInvoice.lineItems.map((item, idx) => (
                        <tr key={idx}>
                          <td className="py-2.5">{item.description}</td>
                          <td className="py-2.5 text-right font-mono font-bold">
                            ${item.amount.toLocaleString()}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                <div className="border-t border-white/10 pt-4 space-y-1.5 text-right">
                  <div className="flex justify-between">
                    <span>Subtotal:</span>
                    <span className="font-mono">${selectedInvoice.subtotal.toLocaleString()}</span>
                  </div>
                  <div className="flex justify-between font-bold text-white text-sm">
                    <span>Total Amount:</span>
                    <span className="font-mono">${selectedInvoice.totalAmount.toLocaleString()}</span>
                  </div>
                  <div className="flex justify-between text-emerald-400">
                    <span>Amount Paid:</span>
                    <span className="font-mono">${selectedInvoice.amountPaid.toLocaleString()}</span>
                  </div>
                  <div className="flex justify-between text-[#D4AF37] font-bold text-sm">
                    <span>Balance Due:</span>
                    <span className="font-mono">${selectedInvoice.balanceDue.toLocaleString()}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* ================= BLOCKED DATES SECTION ================= */}
        {currentSection === 'BLOCKED_DATES' && (
          <div className="space-y-6 animate-fadeIn">
            <div className="p-6 rounded-3xl bg-[#141416] border border-white/5 space-y-4">
              <h3 className="font-serif text-xl font-bold text-white">Block a Calendar Date</h3>
              <p className="text-xs text-[#EBEBEB]/70">
                Blocked dates cannot be chosen by clients in the public booking wizard (e.g. for already booked weddings, equipment maintenance, or vacations).
              </p>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 pt-2">
                <div>
                  <label className="block text-xs font-semibold text-[#D4AF37] uppercase mb-1">
                    Date
                  </label>
                  <input
                    type="date"
                    value={newBlockedDate}
                    onChange={(e) => setNewBlockedDate(e.target.value)}
                    className="w-full px-4 py-2.5 rounded-xl bg-black/40 border border-white/10 text-white text-xs outline-none focus:border-[#D4AF37]"
                  />
                </div>
                <div className="sm:col-span-2">
                  <label className="block text-xs font-semibold text-[#D4AF37] uppercase mb-1">
                    Studio Reason (Shown to client upon selection)
                  </label>
                  <div className="flex gap-2">
                    <input
                      type="text"
                      placeholder="e.g. Fully Booked - Grand Royal Wedding Gala"
                      value={newBlockedReason}
                      onChange={(e) => setNewBlockedReason(e.target.value)}
                      className="flex-1 px-4 py-2.5 rounded-xl bg-black/40 border border-white/10 text-white text-xs outline-none focus:border-[#D4AF37]"
                    />
                    <button
                      type="button"
                      onClick={() => {
                        if (!newBlockedDate) return alert('Please pick a date.');
                        const timestamp = new Date(newBlockedDate).getTime();
                        addBlockedDate(timestamp, newBlockedReason || 'Studio Unavailable');
                        setNewBlockedDate('');
                        setNewBlockedReason('');
                      }}
                      className="px-6 py-2.5 rounded-xl bg-[#D4AF37] text-black font-bold text-xs uppercase"
                    >
                      Block Date
                    </button>
                  </div>
                </div>
              </div>
            </div>

            {/* Blocked dates list */}
            <div className="rounded-2xl bg-[#141416] border border-white/5 overflow-hidden shadow-xl">
              <table className="w-full text-left text-xs">
                <thead className="bg-black/40 text-[#D4AF37] uppercase tracking-wider font-semibold border-b border-white/5">
                  <tr>
                    <th className="p-4">Blocked Date</th>
                    <th className="p-4">Reason</th>
                    <th className="p-4 text-right">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-white/5">
                  {blockedDates.map((b) => (
                    <tr key={b.id} className="hover:bg-white/[0.02]">
                      <td className="p-4 font-bold text-white">
                        {new Date(b.date).toLocaleDateString([], {
                          weekday: 'long',
                          month: 'long',
                          day: 'numeric',
                          year: 'numeric',
                        })}
                      </td>
                      <td className="p-4 text-[#EBEBEB]/80">{b.reason}</td>
                      <td className="p-4 text-right">
                        <button
                          onClick={() => removeBlockedDate(b.id)}
                          className="p-1.5 rounded-lg text-red-400 hover:bg-red-500/10 transition"
                          title="Unblock Date"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* ================= REVIEWS MODERATION SECTION ================= */}
        {currentSection === 'REVIEWS' && (
          <div className="space-y-6 animate-fadeIn">
            <div className="flex items-center justify-between">
              <h3 className="font-serif text-xl font-bold text-white">Client Reviews Moderation</h3>
              <span className="text-xs text-[#EBEBEB]/60">
                {reviews.filter((r) => !r.approved).length} pending moderation
              </span>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {reviews.map((rev) => (
                <div
                  key={rev.id}
                  className={`p-6 rounded-2xl border flex flex-col justify-between space-y-4 ${
                    rev.approved
                      ? 'bg-[#141416] border-white/5'
                      : 'bg-amber-500/5 border-amber-500/30'
                  }`}
                >
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center space-x-1 text-[#D4AF37]">
                        {[...Array(rev.rating)].map((_, i) => (
                          <Star key={i} className="w-4 h-4 fill-[#D4AF37]" />
                        ))}
                      </div>
                      <span
                        className={`px-2 py-0.5 rounded-full text-[10px] font-bold uppercase ${
                          rev.approved
                            ? 'bg-emerald-500/20 text-emerald-400'
                            : 'bg-amber-500/20 text-amber-400'
                        }`}
                      >
                        {rev.approved ? 'Approved' : 'Pending Review'}
                      </span>
                    </div>
                    <p className="text-xs text-[#EBEBEB]/80 italic">"{rev.review}"</p>
                    <p className="text-xs font-bold text-white">
                      {rev.customerName} • <span className="text-[#D4AF37]">{rev.eventType}</span>
                    </p>
                  </div>

                  <div className="pt-3 border-t border-white/10 flex items-center justify-between">
                    <button
                      onClick={() => moderateReview(rev.id, !rev.approved)}
                      className={`px-3 py-1.5 rounded-lg text-xs font-bold transition ${
                        rev.approved
                          ? 'bg-amber-500/20 text-amber-300 hover:bg-amber-500/30'
                          : 'bg-emerald-600 text-white hover:bg-emerald-500'
                      }`}
                    >
                      {rev.approved ? 'Unpublish' : 'Approve & Publish'}
                    </button>

                    <div className="flex items-center space-x-2">
                      <button
                        onClick={() => moderateReview(rev.id, rev.approved, !rev.featured)}
                        className={`px-2.5 py-1.5 rounded-lg text-xs font-medium border ${
                          rev.featured
                            ? 'bg-[#D4AF37] text-black border-[#D4AF37]'
                            : 'border-white/10 text-[#EBEBEB]/60 hover:text-white'
                        }`}
                      >
                        {rev.featured ? 'Featured ★' : 'Feature'}
                      </button>
                      <button
                        onClick={() => deleteReview(rev.id)}
                        className="p-1.5 rounded-lg text-red-400 hover:bg-red-500/10"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* ================= PORTFOLIO SECTION ================= */}
        {currentSection === 'PORTFOLIO' && (
          <div className="space-y-8 animate-fadeIn">
            {/* Add Portfolio Form */}
            <div className="p-6 rounded-3xl bg-[#141416] border border-white/5 space-y-4">
              <h3 className="font-serif text-xl font-bold text-white">Add Portfolio Media Item</h3>
              <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4 text-xs">
                <div>
                  <label className="block text-[#D4AF37] font-semibold mb-1">Title</label>
                  <input
                    type="text"
                    placeholder="e.g. Royal Palace Reception"
                    value={newPortTitle}
                    onChange={(e) => setNewPortTitle(e.target.value)}
                    className="w-full px-3 py-2 rounded-xl bg-black/40 border border-white/10 text-white outline-none focus:border-[#D4AF37]"
                  />
                </div>
                <div>
                  <label className="block text-[#D4AF37] font-semibold mb-1">Category</label>
                  <select
                    value={newPortCat}
                    onChange={(e) => setNewPortCat(e.target.value as PortfolioCategory)}
                    className="w-full px-3 py-2 rounded-xl bg-black/40 border border-white/10 text-white outline-none focus:border-[#D4AF37]"
                  >
                    <option value="WEDDING" className="bg-[#18181B]">Wedding</option>
                    <option value="ENGAGEMENT" className="bg-[#18181B]">Engagement</option>
                    <option value="PRE_WEDDING" className="bg-[#18181B]">Pre-Wedding</option>
                    <option value="BIRTHDAY" className="bg-[#18181B]">Birthday</option>
                    <option value="BABY_FAMILY" className="bg-[#18181B]">Baby & Family</option>
                    <option value="ANNIVERSARY" className="bg-[#18181B]">Anniversary</option>
                    <option value="OTHER_EVENTS" className="bg-[#18181B]">Other Events</option>
                  </select>
                </div>
                <div>
                  <label className="block text-[#D4AF37] font-semibold mb-1">Media Image URL</label>
                  <input
                    type="text"
                    placeholder="/assets/images/service_wedding_...jpg"
                    value={newPortUrl}
                    onChange={(e) => setNewPortUrl(e.target.value)}
                    className="w-full px-3 py-2 rounded-xl bg-black/40 border border-white/10 text-white outline-none focus:border-[#D4AF37]"
                  />
                </div>
                <div className="flex items-end">
                  <button
                    onClick={() => {
                      if (!newPortTitle.trim()) return alert('Title required');
                      addPortfolioItem({
                        title: newPortTitle.trim(),
                        category: newPortCat,
                        type: newPortType,
                        imageUrl: newPortUrl.trim() || '/assets/images/hero_wedding_cinematic_1789061882938.jpg',
                        featured: newPortFeatured,
                        enabled: true,
                      });
                      setNewPortTitle('');
                      setNewPortUrl('');
                    }}
                    className="w-full py-2.5 rounded-xl bg-[#D4AF37] text-black font-bold text-xs uppercase"
                  >
                    Add to Portfolio
                  </button>
                </div>
              </div>
            </div>

            {/* List */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {portfolio.map((item) => (
                <div
                  key={item.id}
                  className="rounded-2xl bg-[#141416] border border-white/5 overflow-hidden flex flex-col justify-between"
                >
                  <div className="aspect-video relative bg-black">
                    <img src={item.imageUrl} alt={item.title} className="w-full h-full object-cover" />
                    <div className="absolute top-2 right-2 flex space-x-1">
                      {item.featured && (
                        <span className="px-2 py-0.5 rounded text-[10px] bg-[#D4AF37] text-black font-bold">
                          Featured
                        </span>
                      )}
                    </div>
                  </div>
                  <div className="p-4 space-y-2">
                    <span className="text-[10px] font-bold text-[#D4AF37] uppercase">
                      {item.category}
                    </span>
                    <h4 className="font-serif text-base font-bold text-white">{item.title}</h4>
                    <div className="flex items-center justify-between pt-2 border-t border-white/5">
                      <button
                        onClick={() =>
                          updatePortfolioItem({ ...item, featured: !item.featured })
                        }
                        className="text-xs text-[#D4AF37] hover:underline"
                      >
                        {item.featured ? 'Remove Feature' : 'Mark Featured'}
                      </button>
                      <button
                        onClick={() => deletePortfolioItem(item.id)}
                        className="p-1 text-red-400 hover:bg-red-500/10 rounded"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* ================= SERVICES & PACKAGES SECTION ================= */}
        {currentSection === 'SERVICES' && (
          <div className="space-y-6 animate-fadeIn">
            <h3 className="font-serif text-xl font-bold text-white">Manage Services</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {services.map((svc) => (
                <div key={svc.id} className="p-6 rounded-2xl bg-[#141416] border border-white/5 space-y-3">
                  <div className="flex items-center justify-between">
                    <h4 className="font-serif text-lg font-bold text-white">{svc.name}</h4>
                    <span className="text-xs font-bold text-[#D4AF37]">{svc.startingPrice}</span>
                  </div>
                  <p className="text-xs text-[#EBEBEB]/70">{svc.shortDescription}</p>
                  <div className="flex items-center justify-between pt-3 border-t border-white/5">
                    <span className="text-[10px] text-emerald-400 font-bold uppercase">
                      {svc.enabled ? 'Active Online' : 'Hidden'}
                    </span>
                    <button
                      onClick={() => updateService({ ...svc, enabled: !svc.enabled })}
                      className="px-3 py-1 rounded-lg text-xs bg-white/5 hover:bg-white/10"
                    >
                      {svc.enabled ? 'Disable' : 'Enable'}
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {currentSection === 'PACKAGES' && (
          <div className="space-y-6 animate-fadeIn">
            <h3 className="font-serif text-xl font-bold text-white">Manage Packages</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {packages.map((pkg) => (
                <div key={pkg.id} className="p-6 rounded-2xl bg-[#141416] border border-white/5 space-y-3">
                  <div className="flex items-center justify-between">
                    <h4 className="font-serif text-lg font-bold text-white">{pkg.name}</h4>
                    <span className="text-sm font-bold text-[#D4AF37]">
                      {pkg.currency}{pkg.price}
                    </span>
                  </div>
                  <p className="text-xs text-[#EBEBEB]/70">{pkg.description}</p>
                  <div className="flex items-center justify-between pt-3 border-t border-white/5">
                    <button
                      onClick={() => updatePackage({ ...pkg, isRecommended: !pkg.isRecommended })}
                      className="text-xs text-[#D4AF37] hover:underline"
                    >
                      {pkg.isRecommended ? 'Recommended (Active)' : 'Mark as Recommended'}
                    </button>
                    <button
                      onClick={() => updatePackage({ ...pkg, enabled: !pkg.enabled })}
                      className="px-3 py-1 rounded-lg text-xs bg-white/5 hover:bg-white/10"
                    >
                      {pkg.enabled ? 'Disable' : 'Enable'}
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* ================= STUDIO SETTINGS SECTION ================= */}
        {currentSection === 'SETTINGS' && (
          <div className="max-w-2xl mx-auto space-y-6 animate-fadeIn">
            <div className="p-8 rounded-3xl bg-[#141416] border border-white/5 space-y-6 shadow-xl">
              <h3 className="font-serif text-2xl font-bold text-white">Studio Configurations</h3>

              {settingsSaved && (
                <div className="p-3.5 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-xs flex items-center space-x-2">
                  <CheckCircle2 className="w-4 h-4" />
                  <span>Studio settings saved and published!</span>
                </div>
              )}

              <div className="space-y-4 text-xs">
                <div>
                  <label className="block text-[#D4AF37] font-semibold mb-1 uppercase tracking-wider">
                    Studio Name
                  </label>
                  <input
                    type="text"
                    value={settingsForm.studioName}
                    onChange={(e) => setSettingsForm({ ...settingsForm, studioName: e.target.value })}
                    className="w-full px-4 py-2.5 rounded-xl bg-black/40 border border-white/10 text-white outline-none focus:border-[#D4AF37]"
                  />
                </div>

                <div>
                  <label className="block text-[#D4AF37] font-semibold mb-1 uppercase tracking-wider">
                    Tagline
                  </label>
                  <input
                    type="text"
                    value={settingsForm.tagline}
                    onChange={(e) => setSettingsForm({ ...settingsForm, tagline: e.target.value })}
                    className="w-full px-4 py-2.5 rounded-xl bg-black/40 border border-white/10 text-white outline-none focus:border-[#D4AF37]"
                  />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-[#D4AF37] font-semibold mb-1 uppercase tracking-wider">
                      Phone Number
                    </label>
                    <input
                      type="text"
                      value={settingsForm.phone}
                      onChange={(e) => setSettingsForm({ ...settingsForm, phone: e.target.value })}
                      className="w-full px-4 py-2.5 rounded-xl bg-black/40 border border-white/10 text-white outline-none focus:border-[#D4AF37]"
                    />
                  </div>
                  <div>
                    <label className="block text-[#D4AF37] font-semibold mb-1 uppercase tracking-wider">
                      WhatsApp Concierge
                    </label>
                    <input
                      type="text"
                      value={settingsForm.whatsapp}
                      onChange={(e) => setSettingsForm({ ...settingsForm, whatsapp: e.target.value })}
                      className="w-full px-4 py-2.5 rounded-xl bg-black/40 border border-white/10 text-white outline-none focus:border-[#D4AF37]"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-[#D4AF37] font-semibold mb-1 uppercase tracking-wider">
                    Studio Address
                  </label>
                  <input
                    type="text"
                    value={settingsForm.address}
                    onChange={(e) => setSettingsForm({ ...settingsForm, address: e.target.value })}
                    className="w-full px-4 py-2.5 rounded-xl bg-black/40 border border-white/10 text-white outline-none focus:border-[#D4AF37]"
                  />
                </div>

                <div>
                  <label className="block text-[#D4AF37] font-semibold mb-1 uppercase tracking-wider">
                    Consultation Hours
                  </label>
                  <textarea
                    rows={2}
                    value={settingsForm.businessHours}
                    onChange={(e) =>
                      setSettingsForm({ ...settingsForm, businessHours: e.target.value })
                    }
                    className="w-full px-4 py-2.5 rounded-xl bg-black/40 border border-white/10 text-white outline-none focus:border-[#D4AF37]"
                  />
                </div>

                <div>
                  <label className="block text-[#D4AF37] font-semibold mb-1 uppercase tracking-wider">
                    About Studio Narrative
                  </label>
                  <textarea
                    rows={4}
                    value={settingsForm.aboutText}
                    onChange={(e) => setSettingsForm({ ...settingsForm, aboutText: e.target.value })}
                    className="w-full px-4 py-2.5 rounded-xl bg-black/40 border border-white/10 text-white outline-none focus:border-[#D4AF37]"
                  />
                </div>
              </div>

              <button
                onClick={() => {
                  updateSettings(settingsForm);
                  setSettingsSaved(true);
                  setTimeout(() => setSettingsSaved(false), 3000);
                }}
                className="w-full py-3.5 rounded-xl bg-[#D4AF37] hover:bg-[#AA8C2C] text-black font-bold text-xs uppercase tracking-wider transition"
              >
                Save & Update Settings
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
