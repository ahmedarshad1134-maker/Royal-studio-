import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { Star, CheckCircle2, MessageSquare, Sparkles, X, PlusCircle, AlertCircle } from 'lucide-react';

export const ReviewsScreen: React.FC = () => {
  const { reviews, submitReview, currentUser, navigateTo } = useApp();
  const [showSubmitModal, setShowSubmitModal] = useState(false);

  // Form state
  const [name, setName] = useState(currentUser?.name || '');
  const [rating, setRating] = useState(5);
  const [eventType, setEventType] = useState('Wedding');
  const [reviewText, setReviewText] = useState('');
  const [formError, setFormError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const approvedReviews = reviews.filter((r) => r.approved);
  const featuredReview = approvedReviews.find((r) => r.featured);
  const averageRating =
    approvedReviews.length > 0
      ? (approvedReviews.reduce((sum, r) => sum + r.rating, 0) / approvedReviews.length).toFixed(1)
      : '5.0';

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError('');

    if (!name.trim()) {
      setFormError('Please enter your name.');
      return;
    }
    if (!reviewText.trim()) {
      setFormError('Please write your review feedback.');
      return;
    }

    try {
      await submitReview(name.trim(), rating, eventType, reviewText.trim());
      setSuccessMsg('Your review has been submitted for studio moderation!');
      setReviewText('');
      setTimeout(() => {
        setSuccessMsg('');
        setShowSubmitModal(false);
      }, 2500);
    } catch {
      setFormError('Failed to submit review. Please try again.');
    }
  };

  return (
    <div className="min-h-screen bg-[#0E0E10] text-[#EBEBEB] py-16 px-4 sm:px-6 lg:px-8">
      <div className="max-w-6xl mx-auto space-y-16">
        {/* Header */}
        <div className="text-center max-w-3xl mx-auto space-y-4">
          <div className="inline-flex items-center space-x-2 px-3 py-1 rounded-full bg-[#D4AF37]/15 border border-[#D4AF37]/30 text-[#D4AF37] text-xs font-semibold uppercase tracking-widest">
            <Sparkles className="w-3.5 h-3.5" />
            <span>Client Testimonials</span>
          </div>
          <h1 className="font-serif text-4xl sm:text-6xl font-bold text-white tracking-tight">
            Stories & Reviews
          </h1>
          <p className="text-sm sm:text-base text-[#EBEBEB]/70 font-light leading-relaxed">
            Read heartfelt experiences from couples, families, and organizations who trusted Royal Studio with their once-in-a-lifetime milestones.
          </p>
        </div>

        {/* Rating Summary Card & Action */}
        <div className="p-8 sm:p-10 rounded-3xl bg-gradient-to-r from-[#18181A] to-[#202025] border border-[#D4AF37]/30 shadow-2xl flex flex-col md:flex-row items-center justify-between gap-8">
          <div className="flex items-center space-x-6 text-center md:text-left">
            <div className="p-4 rounded-2xl bg-black/60 border border-[#D4AF37]/40 text-center shrink-0">
              <span className="font-serif text-5xl font-bold text-[#D4AF37]">{averageRating}</span>
              <div className="flex items-center justify-center space-x-0.5 text-[#D4AF37] mt-1">
                {[...Array(5)].map((_, i) => (
                  <Star key={i} className="w-3.5 h-3.5 fill-[#D4AF37]" />
                ))}
              </div>
            </div>
            <div className="space-y-1">
              <h3 className="font-serif text-2xl font-bold text-white">Exceptional Client Trust</h3>
              <p className="text-xs text-[#EBEBEB]/70">
                Based on verified client reviews from weddings, milestone galas, and pre-wedding sessions.
              </p>
            </div>
          </div>

          <button
            onClick={() => setShowSubmitModal(true)}
            className="w-full md:w-auto px-8 py-3.5 rounded-xl bg-[#D4AF37] hover:bg-[#AA8C2C] text-black font-bold text-xs uppercase tracking-wider transition flex items-center justify-center space-x-2 shadow-lg shadow-[#D4AF37]/20 shrink-0"
          >
            <PlusCircle className="w-4 h-4" />
            <span>Write a Client Review</span>
          </button>
        </div>

        {/* Featured Spotlight Review (if available) */}
        {featuredReview && (
          <div className="p-8 sm:p-12 rounded-3xl bg-gradient-to-b from-[#1F1F24] to-[#141416] border border-[#D4AF37]/40 shadow-2xl relative space-y-6">
            <div className="inline-block px-3 py-1 rounded-full text-[10px] font-extrabold uppercase tracking-widest bg-[#D4AF37] text-black">
              Featured Love Story
            </div>
            <p className="font-serif text-xl sm:text-3xl text-white font-light italic leading-relaxed">
              "{featuredReview.review}"
            </p>
            <div className="flex items-center justify-between pt-4 border-t border-white/10">
              <div>
                <h4 className="font-serif text-lg font-bold text-white">{featuredReview.customerName}</h4>
                <p className="text-xs text-[#D4AF37]">{featuredReview.eventType}</p>
              </div>
              <div className="flex items-center space-x-1 text-[#D4AF37]">
                {[...Array(featuredReview.rating)].map((_, i) => (
                  <Star key={i} className="w-4 h-4 fill-[#D4AF37]" />
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Reviews Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {approvedReviews.map((rev) => (
            <div
              key={rev.id}
              className="p-8 rounded-3xl bg-[#141416] border border-white/5 hover:border-[#D4AF37]/30 flex flex-col justify-between space-y-6 shadow-xl transition-all"
            >
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-1 text-[#D4AF37]">
                    {[...Array(rev.rating)].map((_, i) => (
                      <Star key={i} className="w-4 h-4 fill-[#D4AF37]" />
                    ))}
                  </div>
                  <span className="text-[10px] text-[#EBEBEB]/40">
                    {new Date(rev.createdAt).toLocaleDateString([], {
                      month: 'short',
                      year: 'numeric',
                    })}
                  </span>
                </div>
                <p className="text-xs sm:text-sm text-[#EBEBEB]/80 font-light italic leading-relaxed">
                  "{rev.review}"
                </p>
              </div>

              <div className="pt-4 border-t border-white/5">
                <h4 className="font-serif text-base font-bold text-white">{rev.customerName}</h4>
                <p className="text-xs text-[#D4AF37]">{rev.eventType}</p>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Write a Review Modal */}
      {showSubmitModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/85 backdrop-blur-md animate-fadeIn">
          <div className="relative max-w-lg w-full p-8 rounded-3xl bg-[#18181B] border border-[#D4AF37]/40 shadow-2xl space-y-6">
            <div className="flex items-center justify-between pb-2 border-b border-white/10">
              <h3 className="font-serif text-2xl font-bold text-white">Share Your Experience</h3>
              <button
                onClick={() => setShowSubmitModal(false)}
                className="p-1.5 text-[#EBEBEB]/60 hover:text-white"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {successMsg ? (
              <div className="p-6 rounded-2xl bg-emerald-500/10 border border-emerald-500/30 text-center space-y-2">
                <CheckCircle2 className="w-10 h-10 text-emerald-400 mx-auto" />
                <h4 className="font-bold text-white">Thank You!</h4>
                <p className="text-xs text-emerald-300">{successMsg}</p>
              </div>
            ) : (
              <form onSubmit={handleSubmit} className="space-y-4">
                {formError && (
                  <div className="p-3 rounded-xl bg-red-500/10 border border-red-500/30 text-xs text-red-400 flex items-center space-x-2">
                    <AlertCircle className="w-4 h-4 shrink-0" />
                    <span>{formError}</span>
                  </div>
                )}

                <div>
                  <label className="block text-xs font-semibold text-[#D4AF37] uppercase tracking-wider mb-1">
                    Your Name *
                  </label>
                  <input
                    type="text"
                    required
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    placeholder="e.g. Sarah & Michael Jenkins"
                    className="w-full px-4 py-2.5 rounded-xl bg-black/40 border border-white/10 text-white text-sm outline-none focus:border-[#D4AF37]"
                  />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-semibold text-[#D4AF37] uppercase tracking-wider mb-1">
                      Event Type
                    </label>
                    <select
                      value={eventType}
                      onChange={(e) => setEventType(e.target.value)}
                      className="w-full px-4 py-2.5 rounded-xl bg-black/40 border border-white/10 text-white text-sm outline-none focus:border-[#D4AF37]"
                    >
                      <option value="Wedding" className="bg-[#18181B]">Wedding</option>
                      <option value="Pre-Wedding" className="bg-[#18181B]">Pre-Wedding</option>
                      <option value="Engagement" className="bg-[#18181B]">Engagement</option>
                      <option value="Birthday" className="bg-[#18181B]">Birthday</option>
                      <option value="Baby & Family" className="bg-[#18181B]">Baby & Family</option>
                      <option value="Corporate Gala" className="bg-[#18181B]">Corporate Gala</option>
                      <option value="Anniversary" className="bg-[#18181B]">Anniversary</option>
                    </select>
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-[#D4AF37] uppercase tracking-wider mb-1">
                      Rating
                    </label>
                    <div className="flex items-center space-x-1 py-2">
                      {[1, 2, 3, 4, 5].map((star) => (
                        <button
                          key={star}
                          type="button"
                          onClick={() => setRating(star)}
                          className="p-1 hover:scale-110 transition"
                        >
                          <Star
                            className={`w-5 h-5 ${
                              star <= rating
                                ? 'fill-[#D4AF37] text-[#D4AF37]'
                                : 'text-white/20'
                            }`}
                          />
                        </button>
                      ))}
                    </div>
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-[#D4AF37] uppercase tracking-wider mb-1">
                    Your Review / Testimonial *
                  </label>
                  <textarea
                    rows={4}
                    required
                    value={reviewText}
                    onChange={(e) => setReviewText(e.target.value)}
                    placeholder="Share your thoughts on our visual style, cinematography, crew professionalism, and final deliverables..."
                    className="w-full px-4 py-2.5 rounded-xl bg-black/40 border border-white/10 text-white text-sm outline-none focus:border-[#D4AF37]"
                  />
                </div>

                <div className="pt-2">
                  <button
                    type="submit"
                    className="w-full py-3.5 rounded-xl bg-[#D4AF37] hover:bg-[#AA8C2C] text-black font-bold text-xs uppercase tracking-wider transition shadow-lg shadow-[#D4AF37]/20"
                  >
                    Submit Review for Moderation
                  </button>
                  <p className="text-[10px] text-center text-[#EBEBEB]/40 mt-2">
                    Submitted reviews are reviewed by studio administrators to maintain genuine customer authenticity.
                  </p>
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
