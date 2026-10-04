import React, { useState, useEffect } from 'react';
import { useApp } from '../context/AppContext';
import {
  Calendar as CalendarIcon,
  CheckCircle2,
  Clock,
  MapPin,
  Sparkles,
  User,
  Phone,
  Mail,
  MessageSquare,
  AlertTriangle,
  ArrowRight,
  ArrowLeft,
  Package as PackageIcon,
  Send,
  ExternalLink,
} from 'lucide-react';
import { Booking } from '../types';

export const BookingScreen: React.FC = () => {
  const {
    screenParams,
    packages,
    services,
    blockedDates,
    isDateBlocked,
    createBooking,
    currentUser,
    settings,
    navigateTo,
  } = useApp();

  // Wizard Step (1 to 6)
  const [currentStep, setCurrentStep] = useState<number>(1);
  const totalSteps = 6;

  // Form Fields
  const [eventType, setEventType] = useState<string>('Wedding');
  const [selectedDate, setSelectedDate] = useState<string>(() => {
    // Default to a date 3 weeks from now
    const d = new Date();
    d.setDate(d.getDate() + 21);
    return d.toISOString().split('T')[0];
  });
  const [location, setLocation] = useState<string>('');
  const [selectedPackageId, setSelectedPackageId] = useState<string>(screenParams.packageId || 'pkg_standard');
  const [selectedServiceId, setSelectedServiceId] = useState<string>(screenParams.serviceId || '');
  const [offeringType, setOfferingType] = useState<'PACKAGE' | 'SERVICE'>(
    screenParams.serviceId ? 'SERVICE' : 'PACKAGE'
  );

  const [customerName, setCustomerName] = useState<string>(currentUser?.name || '');
  const [phone, setPhone] = useState<string>(currentUser?.phone || '');
  const [email, setEmail] = useState<string>(currentUser?.email || '');
  const [message, setMessage] = useState<string>('');

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submittedBooking, setSubmittedBooking] = useState<Booking | null>(null);

  // Sync if pre-selected params
  useEffect(() => {
    if (screenParams.packageId) {
      setSelectedPackageId(screenParams.packageId);
      setOfferingType('PACKAGE');
    } else if (screenParams.serviceId) {
      setSelectedServiceId(screenParams.serviceId);
      setOfferingType('SERVICE');
    }
  }, [screenParams]);

  // Check if chosen date is blocked
  const selectedDateMillis = selectedDate ? new Date(selectedDate).getTime() : 0;
  const isSelectedDateBlocked = selectedDateMillis > 0 && isDateBlocked(selectedDateMillis);
  const blockedReason = blockedDates.find((b) => {
    const bDate = new Date(b.date);
    const sel = new Date(selectedDateMillis);
    return (
      bDate.getFullYear() === sel.getFullYear() &&
      bDate.getMonth() === sel.getMonth() &&
      bDate.getDate() === sel.getDate()
    );
  })?.reason;

  // Validation per step
  const handleNextStep = () => {
    const newErrors: Record<string, string> = {};

    if (currentStep === 1) {
      if (!eventType) newErrors.eventType = 'Please select an event type.';
    } else if (currentStep === 2) {
      if (!selectedDate) {
        newErrors.date = 'Please select an event date.';
      } else if (isSelectedDateBlocked) {
        newErrors.date = `This date is currently unavailable: "${blockedReason || 'Studio already fully booked'}". Please select an alternate date.`;
      }
    } else if (currentStep === 3) {
      if (!location.trim()) {
        newErrors.location = 'Please specify the event venue or city.';
      }
    } else if (currentStep === 4) {
      if (offeringType === 'PACKAGE' && !selectedPackageId) {
        newErrors.offering = 'Please select a package.';
      } else if (offeringType === 'SERVICE' && !selectedServiceId) {
        newErrors.offering = 'Please select a service.';
      }
    } else if (currentStep === 5) {
      if (!customerName.trim()) newErrors.customerName = 'Full name is required.';
      if (!phone.trim()) newErrors.phone = 'Phone number is required.';
      if (!email.trim() || !email.includes('@')) newErrors.email = 'Valid email address is required.';
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    setErrors({});

    if (currentStep === 5) {
      // Finalize creation
      const booking = createBooking({
        customerName: customerName.trim(),
        phone: phone.trim(),
        email: email.trim(),
        eventType,
        eventDate: selectedDateMillis,
        eventLocation: location.trim(),
        packageId: offeringType === 'PACKAGE' ? selectedPackageId : undefined,
        serviceId: offeringType === 'SERVICE' ? selectedServiceId : undefined,
        message: message.trim(),
      });
      setSubmittedBooking(booking);
      setCurrentStep(6);
    } else {
      setCurrentStep((prev) => prev + 1);
    }
  };

  const handlePrevStep = () => {
    if (currentStep > 1) {
      setErrors({});
      setCurrentStep((prev) => prev - 1);
    }
  };

  const eventTypes = [
    'Wedding',
    'Engagement',
    'Pre-Wedding',
    'Birthday',
    'Baby / Family',
    'Corporate Event',
    'Anniversary',
    'Other Special Event',
  ];

  const selectedPkg = packages.find((p) => p.id === selectedPackageId);
  const selectedSvc = services.find((s) => s.id === selectedServiceId);

  return (
    <div className="min-h-screen bg-[#0E0E10] text-[#EBEBEB] py-16 px-4 sm:px-6 lg:px-8">
      <div className="max-w-3xl mx-auto space-y-10">
        {/* Header */}
        <div className="text-center space-y-3">
          <div className="inline-flex items-center space-x-2 px-3 py-1 rounded-full bg-[#D4AF37]/15 border border-[#D4AF37]/30 text-[#D4AF37] text-xs font-semibold uppercase tracking-widest">
            <Sparkles className="w-3.5 h-3.5" />
            <span>Interactive Booking Concierge</span>
          </div>
          <h1 className="font-serif text-3xl sm:text-5xl font-bold text-white tracking-tight">
            Reserve Your Date
          </h1>
          <p className="text-xs sm:text-sm text-[#EBEBEB]/70 font-light max-w-lg mx-auto leading-relaxed">
            Follow the 5 simple steps below to check availability and request a royal visual proposal for your celebration.
          </p>
        </div>

        {/* Progress Bar (Steps 1-5) */}
        {currentStep <= 5 && (
          <div className="space-y-2">
            <div className="flex items-center justify-between text-xs font-semibold text-[#EBEBEB]/60">
              <span className="text-[#D4AF37]">
                Step {currentStep} of {totalSteps - 1}
              </span>
              <span>
                {currentStep === 1 && 'Event Type'}
                {currentStep === 2 && 'Event Date & Availability'}
                {currentStep === 3 && 'Venue & Location'}
                {currentStep === 4 && 'Choose Package or Service'}
                {currentStep === 5 && 'Client Contact Details'}
              </span>
            </div>
            <div className="w-full h-2 rounded-full bg-white/5 overflow-hidden">
              <div
                className="h-full bg-gradient-to-r from-[#D4AF37] to-[#AA8C2C] transition-all duration-300"
                style={{ width: `${(currentStep / (totalSteps - 1)) * 100}%` }}
              />
            </div>
          </div>
        )}

        {/* Card Body */}
        <div className="rounded-3xl bg-[#141416] border border-white/5 p-6 sm:p-10 shadow-2xl relative">
          {/* STEP 1: EVENT TYPE */}
          {currentStep === 1 && (
            <div className="space-y-6 animate-fadeIn">
              <div className="space-y-1">
                <h3 className="font-serif text-2xl font-bold text-white">
                  What kind of event are you celebrating?
                </h3>
                <p className="text-xs text-[#EBEBEB]/70">
                  Select your celebration category so we can assign the specialized master crew.
                </p>
              </div>

              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                {eventTypes.map((type) => (
                  <button
                    key={type}
                    type="button"
                    onClick={() => {
                      setEventType(type);
                      setErrors({});
                    }}
                    className={`p-4 rounded-2xl text-xs font-bold transition flex flex-col items-center justify-center text-center space-y-2 border ${
                      eventType === type
                        ? 'bg-[#D4AF37] text-black border-[#D4AF37] shadow-lg shadow-[#D4AF37]/20 scale-102'
                        : 'bg-white/5 text-[#EBEBEB]/80 border-white/5 hover:border-[#D4AF37]/30 hover:bg-white/10'
                    }`}
                  >
                    <span>{type}</span>
                  </button>
                ))}
              </div>

              {errors.eventType && (
                <p className="text-xs text-red-400 font-medium">{errors.eventType}</p>
              )}
            </div>
          )}

          {/* STEP 2: EVENT DATE & AVAILABILITY */}
          {currentStep === 2 && (
            <div className="space-y-6 animate-fadeIn">
              <div className="space-y-1">
                <h3 className="font-serif text-2xl font-bold text-white">
                  When is your special date?
                </h3>
                <p className="text-xs text-[#EBEBEB]/70">
                  Our system verifies against the studio calendar to ensure master crew availability.
                </p>
              </div>

              <div className="space-y-4">
                <label className="block text-xs font-semibold uppercase tracking-wider text-[#D4AF37]">
                  Select Date
                </label>
                <div className="relative">
                  <input
                    type="date"
                    value={selectedDate}
                    min={new Date().toISOString().split('T')[0]}
                    onChange={(e) => {
                      setSelectedDate(e.target.value);
                      setErrors({});
                    }}
                    className="w-full px-4 py-3.5 rounded-xl bg-black/40 border border-white/10 focus:border-[#D4AF37] text-white text-sm outline-none transition"
                  />
                  <CalendarIcon className="w-5 h-5 text-[#D4AF37] absolute right-4 top-1/2 -translate-y-1/2 pointer-events-none" />
                </div>

                {/* Blocked Date Warning Alert */}
                {isSelectedDateBlocked && (
                  <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/30 text-red-400 text-xs flex items-start space-x-3">
                    <AlertTriangle className="w-5 h-5 shrink-0 mt-0.5" />
                    <div>
                      <p className="font-bold">Studio Date Unavailable</p>
                      <p className="mt-0.5 text-red-300">
                        "{blockedReason || 'This date is blocked for a private royal booking'}". Please choose another date or contact our concierge directly.
                      </p>
                    </div>
                  </div>
                )}

                {!isSelectedDateBlocked && selectedDate && (
                  <div className="p-4 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-xs flex items-center space-x-2">
                    <CheckCircle2 className="w-4 h-4 shrink-0" />
                    <span>Date is currently open for booking reservation!</span>
                  </div>
                )}

                {errors.date && (
                  <p className="text-xs text-red-400 font-medium">{errors.date}</p>
                )}
              </div>
            </div>
          )}

          {/* STEP 3: VENUE & LOCATION */}
          {currentStep === 3 && (
            <div className="space-y-6 animate-fadeIn">
              <div className="space-y-1">
                <h3 className="font-serif text-2xl font-bold text-white">
                  Where will the celebration take place?
                </h3>
                <p className="text-xs text-[#EBEBEB]/70">
                  Provide the venue name, city, or destination so we can coordinate permits and travel logistics.
                </p>
              </div>

              <div className="space-y-3">
                <label className="block text-xs font-semibold uppercase tracking-wider text-[#D4AF37]">
                  Venue / Location / Destination *
                </label>
                <div className="relative">
                  <input
                    type="text"
                    value={location}
                    onChange={(e) => {
                      setLocation(e.target.value);
                      setErrors({});
                    }}
                    placeholder="e.g. The Grand Heritage Ballroom, Sunset Valley"
                    className="w-full px-4 py-3.5 pl-11 rounded-xl bg-black/40 border border-white/10 focus:border-[#D4AF37] text-white text-sm outline-none transition"
                  />
                  <MapPin className="w-4 h-4 text-[#D4AF37] absolute left-4 top-1/2 -translate-y-1/2" />
                </div>
                {errors.location && (
                  <p className="text-xs text-red-400 font-medium">{errors.location}</p>
                )}
              </div>
            </div>
          )}

          {/* STEP 4: PACKAGE OR SERVICE SELECTION */}
          {currentStep === 4 && (
            <div className="space-y-6 animate-fadeIn">
              <div className="space-y-1">
                <h3 className="font-serif text-2xl font-bold text-white">
                  Choose Your Visual Package
                </h3>
                <p className="text-xs text-[#EBEBEB]/70">
                  Select an all-inclusive signature package or an a la carte service.
                </p>
              </div>

              {/* Tabs */}
              <div className="flex p-1 rounded-xl bg-black/40 border border-white/10 max-w-sm">
                <button
                  type="button"
                  onClick={() => setOfferingType('PACKAGE')}
                  className={`flex-1 py-2 text-xs font-bold rounded-lg transition ${
                    offeringType === 'PACKAGE'
                      ? 'bg-[#D4AF37] text-black shadow-md'
                      : 'text-[#EBEBEB]/70 hover:text-white'
                  }`}
                >
                  Signature Packages
                </button>
                <button
                  type="button"
                  onClick={() => setOfferingType('SERVICE')}
                  className={`flex-1 py-2 text-xs font-bold rounded-lg transition ${
                    offeringType === 'SERVICE'
                      ? 'bg-[#D4AF37] text-black shadow-md'
                      : 'text-[#EBEBEB]/70 hover:text-white'
                  }`}
                >
                  A La Carte Services
                </button>
              </div>

              {offeringType === 'PACKAGE' ? (
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  {packages
                    .filter((p) => p.enabled)
                    .map((pkg) => (
                      <div
                        key={pkg.id}
                        onClick={() => setSelectedPackageId(pkg.id)}
                        className={`p-5 rounded-2xl border cursor-pointer transition ${
                          selectedPackageId === pkg.id
                            ? 'bg-[#D4AF37]/10 border-[#D4AF37] ring-1 ring-[#D4AF37]'
                            : 'bg-white/[0.02] border-white/5 hover:border-white/20'
                        }`}
                      >
                        <div className="flex items-center justify-between">
                          <h4 className="font-serif text-lg font-bold text-white">{pkg.name}</h4>
                          <span className="text-sm font-bold text-[#D4AF37]">
                            {pkg.currency}
                            {pkg.price}
                          </span>
                        </div>
                        <p className="text-xs text-[#EBEBEB]/70 mt-1 line-clamp-2">{pkg.description}</p>
                        <div className="mt-3 text-[11px] text-[#EBEBEB]/60 flex items-center space-x-1">
                          <Clock className="w-3 h-3 text-[#D4AF37]" />
                          <span>{pkg.photographyHours}</span>
                        </div>
                      </div>
                    ))}
                </div>
              ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  {services
                    .filter((s) => s.enabled)
                    .map((svc) => (
                      <div
                        key={svc.id}
                        onClick={() => setSelectedServiceId(svc.id)}
                        className={`p-5 rounded-2xl border cursor-pointer transition ${
                          selectedServiceId === svc.id
                            ? 'bg-[#D4AF37]/10 border-[#D4AF37] ring-1 ring-[#D4AF37]'
                            : 'bg-white/[0.02] border-white/5 hover:border-white/20'
                        }`}
                      >
                        <div className="flex items-center justify-between">
                          <h4 className="font-serif text-lg font-bold text-white">{svc.name}</h4>
                          <span className="text-xs font-bold text-[#D4AF37]">
                            From {svc.startingPrice}
                          </span>
                        </div>
                        <p className="text-xs text-[#EBEBEB]/70 mt-1 line-clamp-2">{svc.shortDescription}</p>
                      </div>
                    ))}
                </div>
              )}

              {errors.offering && (
                <p className="text-xs text-red-400 font-medium">{errors.offering}</p>
              )}
            </div>
          )}

          {/* STEP 5: CONTACT INFORMATION */}
          {currentStep === 5 && (
            <div className="space-y-6 animate-fadeIn">
              <div className="space-y-1">
                <h3 className="font-serif text-2xl font-bold text-white">
                  Your Contact Information
                </h3>
                <p className="text-xs text-[#EBEBEB]/70">
                  Where should we send your booking itinerary and formal quotation?
                </p>
              </div>

              <div className="space-y-4">
                <div>
                  <label className="block text-xs font-semibold uppercase tracking-wider text-[#D4AF37] mb-1.5">
                    Full Name *
                  </label>
                  <div className="relative">
                    <input
                      type="text"
                      value={customerName}
                      onChange={(e) => {
                        setCustomerName(e.target.value);
                        setErrors({});
                      }}
                      placeholder="e.g. Sarah Jenkins"
                      className="w-full px-4 py-3 pl-11 rounded-xl bg-black/40 border border-white/10 focus:border-[#D4AF37] text-white text-sm outline-none"
                    />
                    <User className="w-4 h-4 text-[#D4AF37] absolute left-4 top-1/2 -translate-y-1/2" />
                  </div>
                  {errors.customerName && (
                    <p className="text-xs text-red-400 mt-1">{errors.customerName}</p>
                  )}
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-semibold uppercase tracking-wider text-[#D4AF37] mb-1.5">
                      Phone Number *
                    </label>
                    <div className="relative">
                      <input
                        type="tel"
                        value={phone}
                        onChange={(e) => {
                          setPhone(e.target.value);
                          setErrors({});
                        }}
                        placeholder="e.g. +1 555-019-2831"
                        className="w-full px-4 py-3 pl-11 rounded-xl bg-black/40 border border-white/10 focus:border-[#D4AF37] text-white text-sm outline-none"
                      />
                      <Phone className="w-4 h-4 text-[#D4AF37] absolute left-4 top-1/2 -translate-y-1/2" />
                    </div>
                    {errors.phone && (
                      <p className="text-xs text-red-400 mt-1">{errors.phone}</p>
                    )}
                  </div>

                  <div>
                    <label className="block text-xs font-semibold uppercase tracking-wider text-[#D4AF37] mb-1.5">
                      Email Address *
                    </label>
                    <div className="relative">
                      <input
                        type="email"
                        value={email}
                        onChange={(e) => {
                          setEmail(e.target.value);
                          setErrors({});
                        }}
                        placeholder="e.g. sarah.jenkins@example.com"
                        className="w-full px-4 py-3 pl-11 rounded-xl bg-black/40 border border-white/10 focus:border-[#D4AF37] text-white text-sm outline-none"
                      />
                      <Mail className="w-4 h-4 text-[#D4AF37] absolute left-4 top-1/2 -translate-y-1/2" />
                    </div>
                    {errors.email && (
                      <p className="text-xs text-red-400 mt-1">{errors.email}</p>
                    )}
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold uppercase tracking-wider text-[#D4AF37] mb-1.5">
                    Special Requests or Event Vision (Optional)
                  </label>
                  <div className="relative">
                    <textarea
                      rows={3}
                      value={message}
                      onChange={(e) => setMessage(e.target.value)}
                      placeholder="Share estimated guest count, preferred style, schedule, or questions..."
                      className="w-full px-4 py-3 pl-11 rounded-xl bg-black/40 border border-white/10 focus:border-[#D4AF37] text-white text-sm outline-none"
                    />
                    <MessageSquare className="w-4 h-4 text-[#D4AF37] absolute left-4 top-4" />
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* STEP 6: CONFIRMATION & REFERENCE */}
          {currentStep === 6 && submittedBooking && (
            <div className="text-center space-y-6 py-6 animate-fadeIn">
              <div className="w-16 h-16 rounded-full bg-[#D4AF37]/20 border-2 border-[#D4AF37] flex items-center justify-center mx-auto text-[#D4AF37]">
                <CheckCircle2 className="w-8 h-8" />
              </div>

              <div className="space-y-2">
                <span className="text-xs uppercase tracking-widest text-[#D4AF37] font-bold">
                  Enquiry Registered Successfully
                </span>
                <h3 className="font-serif text-3xl font-bold text-white">
                  We Look Forward to Your Celebration!
                </h3>
                <p className="text-xs sm:text-sm text-[#EBEBEB]/70 max-w-md mx-auto leading-relaxed">
                  Your reference ID is <span className="text-[#D4AF37] font-bold">#{submittedBooking.referenceId}</span>. A producer will contact you within 24 business hours.
                </p>
              </div>

              {/* Summary Card */}
              <div className="max-w-md mx-auto p-5 rounded-2xl bg-black/50 border border-[#D4AF37]/30 text-left text-xs space-y-2.5">
                <div className="flex justify-between border-b border-white/10 pb-2">
                  <span className="text-[#EBEBEB]/60">Booking Reference</span>
                  <span className="font-bold text-[#D4AF37]">#{submittedBooking.referenceId}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-[#EBEBEB]/60">Event</span>
                  <span className="font-semibold text-white">{submittedBooking.eventType}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-[#EBEBEB]/60">Date</span>
                  <span className="font-semibold text-white">
                    {new Date(submittedBooking.eventDate).toLocaleDateString([], {
                      weekday: 'short',
                      month: 'long',
                      day: 'numeric',
                      year: 'numeric',
                    })}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-[#EBEBEB]/60">Location</span>
                  <span className="font-semibold text-white">{submittedBooking.eventLocation}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-[#EBEBEB]/60">Client</span>
                  <span className="font-semibold text-white">{submittedBooking.customerName}</span>
                </div>
              </div>

              {/* WhatsApp direct concierge button */}
              <div className="pt-2 flex flex-col sm:flex-row items-center justify-center gap-3">
                <a
                  href={`https://wa.me/${settings.whatsapp.replace(/[^0-9]/g, '')}?text=${encodeURIComponent(
                    `Hello Royal Studio, I have submitted booking enquiry #${submittedBooking.referenceId} for ${submittedBooking.eventType} on ${new Date(submittedBooking.eventDate).toLocaleDateString()}. My name is ${submittedBooking.customerName}.`
                  )}`}
                  target="_blank"
                  rel="noreferrer"
                  className="w-full sm:w-auto px-6 py-3 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs uppercase tracking-wider transition flex items-center justify-center space-x-2"
                >
                  <MessageSquare className="w-4 h-4" />
                  <span>Connect Instantly on WhatsApp</span>
                  <ExternalLink className="w-3.5 h-3.5" />
                </a>

                <button
                  onClick={() => navigateTo('customer_area')}
                  className="w-full sm:w-auto px-6 py-3 rounded-xl bg-[#D4AF37] hover:bg-[#AA8C2C] text-black font-bold text-xs uppercase tracking-wider transition"
                >
                  Go to Client Portal
                </button>
              </div>
            </div>
          )}

          {/* Navigation Controls (Steps 1 to 5) */}
          {currentStep <= 5 && (
            <div className="flex items-center justify-between pt-8 border-t border-white/10 mt-8">
              {currentStep > 1 ? (
                <button
                  type="button"
                  onClick={handlePrevStep}
                  className="px-5 py-2.5 rounded-xl border border-white/15 text-[#EBEBEB]/80 hover:bg-white/5 font-semibold text-xs transition flex items-center space-x-1.5"
                >
                  <ArrowLeft className="w-3.5 h-3.5" />
                  <span>Previous</span>
                </button>
              ) : (
                <div />
              )}

              <button
                type="button"
                onClick={handleNextStep}
                disabled={currentStep === 2 && isSelectedDateBlocked}
                className="px-8 py-3 rounded-xl bg-[#D4AF37] hover:bg-[#AA8C2C] disabled:opacity-50 disabled:cursor-not-allowed text-black font-bold text-xs uppercase tracking-wider shadow-lg shadow-[#D4AF37]/20 transition flex items-center space-x-2"
              >
                <span>{currentStep === 5 ? 'Confirm & Submit Enquiry' : 'Continue'}</span>
                <ArrowRight className="w-3.5 h-3.5" />
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
