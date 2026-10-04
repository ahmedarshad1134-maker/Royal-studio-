import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import {
  Phone,
  MessageSquare,
  Mail,
  MapPin,
  Clock,
  Send,
  CheckCircle2,
  AlertCircle,
  ExternalLink,
  Sparkles,
} from 'lucide-react';

export const ContactScreen: React.FC = () => {
  const { settings, submitContactMessage } = useApp();

  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const newErrors: Record<string, string> = {};

    if (!name.trim()) newErrors.name = 'Full name is required.';
    if (!phone.trim()) newErrors.phone = 'Phone number is required.';
    if (email && !email.includes('@')) newErrors.email = 'Please provide a valid email.';
    if (!message.trim()) newErrors.message = 'Message cannot be empty.';

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    setErrors({});
    setIsSubmitting(true);

    try {
      await submitContactMessage(name.trim(), phone.trim(), email.trim(), message.trim());
      setIsSubmitting(false);
      setSuccess(true);
      setName('');
      setPhone('');
      setEmail('');
      setMessage('');
      setTimeout(() => setSuccess(false), 5000);
    } catch {
      setIsSubmitting(false);
      setErrors({ form: 'Failed to send message. Please try again or WhatsApp us.' });
    }
  };

  return (
    <div className="min-h-screen bg-[#0E0E10] text-[#EBEBEB] py-16 px-4 sm:px-6 lg:px-8">
      <div className="max-w-6xl mx-auto space-y-16">
        {/* Header */}
        <div className="text-center max-w-3xl mx-auto space-y-4">
          <div className="inline-flex items-center space-x-2 px-3 py-1 rounded-full bg-[#D4AF37]/15 border border-[#D4AF37]/30 text-[#D4AF37] text-xs font-semibold uppercase tracking-widest">
            <Sparkles className="w-3.5 h-3.5" />
            <span>Direct Concierge</span>
          </div>
          <h1 className="font-serif text-4xl sm:text-6xl font-bold text-white tracking-tight">
            Connect With Our Studio
          </h1>
          <p className="text-sm sm:text-base text-[#EBEBEB]/70 font-light leading-relaxed">
            Whether inquiring about wedding dates, destination packages, or private portrait sessions, our studio producers are here to assist.
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-12">
          {/* Left Column: Direct Info & Quick Channels */}
          <div className="lg:col-span-5 space-y-8">
            <div className="p-8 rounded-3xl bg-[#141416] border border-white/5 space-y-6 shadow-xl">
              <h3 className="font-serif text-2xl font-bold text-white">Studio Channels</h3>

              <div className="space-y-4">
                {/* WhatsApp Direct */}
                <a
                  href={`https://wa.me/${settings.whatsapp.replace(/[^0-9]/g, '')}?text=${encodeURIComponent(
                    'Hello Royal Studio, I would like to inquire about event photography and cinematography.'
                  )}`}
                  target="_blank"
                  rel="noreferrer"
                  className="flex items-center justify-between p-4 rounded-2xl bg-emerald-600/10 hover:bg-emerald-600/20 border border-emerald-500/30 text-emerald-400 transition group"
                >
                  <div className="flex items-center space-x-3">
                    <div className="p-2.5 rounded-xl bg-emerald-600 text-white">
                      <MessageSquare className="w-5 h-5" />
                    </div>
                    <div>
                      <p className="text-xs font-bold text-white">WhatsApp Concierge</p>
                      <p className="text-[11px] text-emerald-300">{settings.whatsapp}</p>
                    </div>
                  </div>
                  <ExternalLink className="w-4 h-4 text-emerald-400 group-hover:translate-x-0.5 transition" />
                </a>

                {/* Direct Call */}
                <a
                  href={`tel:${settings.phone}`}
                  className="flex items-center space-x-3 p-4 rounded-2xl bg-white/[0.02] hover:bg-white/5 border border-white/5 transition"
                >
                  <div className="p-2.5 rounded-xl bg-[#D4AF37]/15 text-[#D4AF37]">
                    <Phone className="w-5 h-5" />
                  </div>
                  <div>
                    <p className="text-xs font-bold text-white">Direct Phone</p>
                    <p className="text-[11px] text-[#EBEBEB]/70">{settings.phone}</p>
                  </div>
                </a>

                {/* Email */}
                <a
                  href={`mailto:${settings.email}`}
                  className="flex items-center space-x-3 p-4 rounded-2xl bg-white/[0.02] hover:bg-white/5 border border-white/5 transition"
                >
                  <div className="p-2.5 rounded-xl bg-[#D4AF37]/15 text-[#D4AF37]">
                    <Mail className="w-5 h-5" />
                  </div>
                  <div>
                    <p className="text-xs font-bold text-white">Official Inquiries</p>
                    <p className="text-[11px] text-[#EBEBEB]/70">{settings.email}</p>
                  </div>
                </a>

                {/* Address */}
                <div className="flex items-start space-x-3 p-4 rounded-2xl bg-white/[0.02] border border-white/5">
                  <div className="p-2.5 rounded-xl bg-[#D4AF37]/15 text-[#D4AF37] shrink-0 mt-0.5">
                    <MapPin className="w-5 h-5" />
                  </div>
                  <div>
                    <p className="text-xs font-bold text-white">Studio Headquarters</p>
                    <p className="text-[11px] text-[#EBEBEB]/70 mt-0.5">{settings.address}</p>
                  </div>
                </div>

                {/* Hours */}
                <div className="flex items-start space-x-3 p-4 rounded-2xl bg-white/[0.02] border border-white/5">
                  <div className="p-2.5 rounded-xl bg-[#D4AF37]/15 text-[#D4AF37] shrink-0 mt-0.5">
                    <Clock className="w-5 h-5" />
                  </div>
                  <div>
                    <p className="text-xs font-bold text-white">Consultation Hours</p>
                    <p className="text-[11px] text-[#EBEBEB]/70 mt-0.5 whitespace-pre-line">
                      {settings.businessHours}
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Right Column: Interactive Contact Form */}
          <div className="lg:col-span-7">
            <div className="p-8 sm:p-10 rounded-3xl bg-[#141416] border border-white/5 space-y-6 shadow-2xl">
              <div className="space-y-1">
                <h3 className="font-serif text-2xl font-bold text-white">Send an Online Message</h3>
                <p className="text-xs text-[#EBEBEB]/70">
                  Fill in the message form below and our studio coordinator will respond shortly.
                </p>
              </div>

              {success && (
                <div className="p-4 rounded-2xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-xs flex items-center space-x-3">
                  <CheckCircle2 className="w-5 h-5 shrink-0" />
                  <span>Your message has been delivered to Royal Studio! We will reach out promptly.</span>
                </div>
              )}

              {errors.form && (
                <div className="p-4 rounded-2xl bg-red-500/10 border border-red-500/30 text-red-400 text-xs flex items-center space-x-3">
                  <AlertCircle className="w-5 h-5 shrink-0" />
                  <span>{errors.form}</span>
                </div>
              )}

              <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                  <label className="block text-xs font-semibold text-[#D4AF37] uppercase tracking-wider mb-1">
                    Your Full Name *
                  </label>
                  <input
                    type="text"
                    value={name}
                    onChange={(e) => {
                      setName(e.target.value);
                      if (errors.name) setErrors({ ...errors, name: '' });
                    }}
                    placeholder="e.g. Eleanor Vance"
                    className="w-full px-4 py-3 rounded-xl bg-black/40 border border-white/10 text-white text-sm outline-none focus:border-[#D4AF37]"
                  />
                  {errors.name && <p className="text-xs text-red-400 mt-1">{errors.name}</p>}
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-semibold text-[#D4AF37] uppercase tracking-wider mb-1">
                      Phone Number *
                    </label>
                    <input
                      type="tel"
                      value={phone}
                      onChange={(e) => {
                        setPhone(e.target.value);
                        if (errors.phone) setErrors({ ...errors, phone: '' });
                      }}
                      placeholder="+1 (555) 000-0000"
                      className="w-full px-4 py-3 rounded-xl bg-black/40 border border-white/10 text-white text-sm outline-none focus:border-[#D4AF37]"
                    />
                    {errors.phone && <p className="text-xs text-red-400 mt-1">{errors.phone}</p>}
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-[#D4AF37] uppercase tracking-wider mb-1">
                      Email Address
                    </label>
                    <input
                      type="email"
                      value={email}
                      onChange={(e) => {
                        setEmail(e.target.value);
                        if (errors.email) setErrors({ ...errors, email: '' });
                      }}
                      placeholder="client@example.com"
                      className="w-full px-4 py-3 rounded-xl bg-black/40 border border-white/10 text-white text-sm outline-none focus:border-[#D4AF37]"
                    />
                    {errors.email && <p className="text-xs text-red-400 mt-1">{errors.email}</p>}
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-[#D4AF37] uppercase tracking-wider mb-1">
                    Your Message / Inquiry *
                  </label>
                  <textarea
                    rows={4}
                    value={message}
                    onChange={(e) => {
                      setMessage(e.target.value);
                      if (errors.message) setErrors({ ...errors, message: '' });
                    }}
                    placeholder="Tell us about your celebration, date, location, or questions..."
                    className="w-full px-4 py-3 rounded-xl bg-black/40 border border-white/10 text-white text-sm outline-none focus:border-[#D4AF37]"
                  />
                  {errors.message && <p className="text-xs text-red-400 mt-1">{errors.message}</p>}
                </div>

                <div className="pt-2">
                  <button
                    type="submit"
                    disabled={isSubmitting}
                    className="w-full py-3.5 rounded-xl bg-[#D4AF37] hover:bg-[#AA8C2C] disabled:opacity-50 text-black font-bold text-xs uppercase tracking-wider transition shadow-lg shadow-[#D4AF37]/20 flex items-center justify-center space-x-2"
                  >
                    <Send className="w-4 h-4" />
                    <span>{isSubmitting ? 'Sending...' : 'Send Message'}</span>
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
