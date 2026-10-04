import React from 'react';
import { useApp } from '../context/AppContext';
import { Award, Camera, Film, Sparkles, Heart, Users, CheckCircle2 } from 'lucide-react';

export const AboutScreen: React.FC = () => {
  const { settings, navigateTo } = useApp();

  const teamMembers = [
    {
      id: 'team_1',
      name: 'Ahmed Arshad',
      role: 'Founder & Lead Cinematographer',
      bio: 'With over a decade of cinematic storytelling behind the lens, Ahmed merges Hollywood visual pacing with deep emotive framing to turn weddings into unforgettable films.',
      image: '/assets/images/service_wedding_1789061903404.jpg',
    },
    {
      id: 'team_2',
      name: 'Elena Rostova',
      role: 'Master Fine-Art Photographer',
      bio: 'Specializing in editorial composition and natural light, Elena captures raw human vulnerability, timeless romance, and subtle gestures with classical elegance.',
      image: '/assets/images/portfolio_sample_1789061922601.jpg',
    },
  ];

  const milestones = [
    { year: '2014', title: 'Studio Founded', desc: 'Established with a focus on editorial wedding photography and documentary cinematography.' },
    { year: '2018', title: 'Cinematography Award', desc: 'Honored with Best Wedding Film Feature for destination storytelling across Europe and Asia.' },
    { year: '2022', title: 'Private Deliverables Portal', desc: 'Pioneered custom client cloud delivery suites with 4K streaming and instant mobile galleries.' },
    { year: '2026', title: '1,000+ Celebrations Captured', desc: 'Over 1,000 royal weddings, intimate elopements, and high-profile galas preserved with prestige.' },
  ];

  return (
    <div className="min-h-screen bg-[#0E0E10] text-[#EBEBEB] py-16 px-4 sm:px-6 lg:px-8">
      <div className="max-w-6xl mx-auto space-y-20">
        {/* Header */}
        <div className="text-center max-w-3xl mx-auto space-y-4">
          <div className="inline-flex items-center space-x-2 px-3 py-1 rounded-full bg-[#D4AF37]/15 border border-[#D4AF37]/30 text-[#D4AF37] text-xs font-semibold uppercase tracking-widest">
            <Sparkles className="w-3.5 h-3.5" />
            <span>Heritage & Craftsmanship</span>
          </div>
          <h1 className="font-serif text-4xl sm:text-6xl font-bold text-white tracking-tight">
            The Royal Studio Story
          </h1>
          <p className="text-sm sm:text-base text-[#EBEBEB]/70 font-light leading-relaxed">
            {settings.tagline} — founded on the belief that human love is regal, sacred, and deserves to be recorded as an immortal work of art.
          </p>
        </div>

        {/* Hero Narrative with Imagery */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
          <div className="relative rounded-3xl overflow-hidden aspect-[4/3] bg-black border border-[#D4AF37]/30 shadow-2xl">
            <img
              src="/assets/images/hero_wedding_cinematic_1789061882938.jpg"
              alt="Royal Studio Founders"
              className="w-full h-full object-cover"
            />
            <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-transparent" />
            <div className="absolute bottom-6 left-6 right-6 p-4 rounded-xl bg-black/60 backdrop-blur-md border border-white/10 text-xs text-[#EBEBEB]/80 font-light">
              "We do not merely take pictures; we preserve the emotional cadence of your lifetime."
            </div>
          </div>

          <div className="space-y-6">
            <h2 className="font-serif text-3xl sm:text-4xl font-bold text-white">
              A Decade of Fine-Art Visual Mastery
            </h2>
            <p className="text-sm text-[#EBEBEB]/80 font-light leading-relaxed">
              {settings.aboutText}
            </p>
            <p className="text-sm text-[#EBEBEB]/80 font-light leading-relaxed">
              Our team consists of passionate filmmakers, master retouchers, and award-winning photographers who balance quiet observation with editorial direction. Whether documenting quiet morning vows or a grand ballroom celebration with hundreds of guests, our lens remains focused on raw sincerity.
            </p>

            <div className="grid grid-cols-2 gap-4 pt-2">
              <div className="p-4 rounded-2xl bg-[#141416] border border-white/5 space-y-1">
                <p className="font-serif text-3xl font-bold text-[#D4AF37]">10+ Years</p>
                <p className="text-xs text-[#EBEBEB]/60">Dedicated Industry Heritage</p>
              </div>
              <div className="p-4 rounded-2xl bg-[#141416] border border-white/5 space-y-1">
                <p className="font-serif text-3xl font-bold text-[#D4AF37]">1,000+</p>
                <p className="text-xs text-[#EBEBEB]/60">Cherished Celebrations</p>
              </div>
            </div>
          </div>
        </div>

        {/* Studio Philosophy & Pillars */}
        <div className="space-y-8">
          <div className="text-center max-w-2xl mx-auto space-y-2">
            <span className="text-xs text-[#D4AF37] uppercase tracking-widest font-semibold">
              Our Core Tenets
            </span>
            <h3 className="font-serif text-3xl font-bold text-white">The Artistic Philosophy</h3>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {[
              {
                icon: Heart,
                title: 'Authentic Emotion First',
                desc: 'We never force artificial poses. Our documentary approach allows genuine tears, laughter, and embrace to unfold naturally.',
              },
              {
                icon: Camera,
                title: 'Cinema Optics & Color Depth',
                desc: 'We shoot on specialized large-format cinema sensors and hand-grade every reel for organic filmic tones and rich royal skin tones.',
              },
              {
                icon: Award,
                title: 'Archival Permanence',
                desc: 'From Italian leather heirloom books to encrypted 4K cloud vaults, our deliverables are created to survive for future generations.',
              },
            ].map((pillar, idx) => (
              <div
                key={idx}
                className="p-8 rounded-3xl bg-[#141416] border border-white/5 hover:border-[#D4AF37]/30 transition-all space-y-4"
              >
                <div className="p-3 rounded-2xl bg-[#D4AF37]/10 text-[#D4AF37] w-fit">
                  <pillar.icon className="w-6 h-6" />
                </div>
                <h4 className="font-serif text-xl font-bold text-white">{pillar.title}</h4>
                <p className="text-xs sm:text-sm text-[#EBEBEB]/70 font-light leading-relaxed">
                  {pillar.desc}
                </p>
              </div>
            ))}
          </div>
        </div>

        {/* Team Members */}
        <div className="space-y-8">
          <div className="text-center max-w-2xl mx-auto space-y-2">
            <span className="text-xs text-[#D4AF37] uppercase tracking-widest font-semibold">
              The Creators
            </span>
            <h3 className="font-serif text-3xl font-bold text-white">Meet Our Lead Artists</h3>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
            {teamMembers.map((member) => (
              <div
                key={member.id}
                className="p-6 rounded-3xl bg-[#141416] border border-white/5 flex flex-col sm:flex-row gap-6 items-center"
              >
                <div className="w-32 h-32 rounded-2xl overflow-hidden shrink-0 border border-[#D4AF37]/30 bg-black">
                  <img
                    src={member.image}
                    alt={member.name}
                    className="w-full h-full object-cover"
                  />
                </div>
                <div className="space-y-2 text-center sm:text-left">
                  <span className="text-[10px] text-[#D4AF37] font-bold uppercase tracking-wider">
                    {member.role}
                  </span>
                  <h4 className="font-serif text-xl font-bold text-white">{member.name}</h4>
                  <p className="text-xs text-[#EBEBEB]/70 font-light leading-relaxed">
                    {member.bio}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Milestones Timeline */}
        <div className="space-y-8">
          <div className="text-center max-w-2xl mx-auto space-y-2">
            <span className="text-xs text-[#D4AF37] uppercase tracking-widest font-semibold">
              Chronicles
            </span>
            <h3 className="font-serif text-3xl font-bold text-white">Studio Milestones</h3>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {milestones.map((m, idx) => (
              <div
                key={idx}
                className="p-6 rounded-3xl bg-[#141416] border border-white/5 space-y-2"
              >
                <span className="font-serif text-2xl font-bold text-[#D4AF37]">{m.year}</span>
                <h4 className="text-sm font-bold text-white">{m.title}</h4>
                <p className="text-xs text-[#EBEBEB]/60 font-light leading-relaxed">{m.desc}</p>
              </div>
            ))}
          </div>
        </div>

        {/* CTA */}
        <div className="p-8 sm:p-12 rounded-3xl bg-gradient-to-r from-[#18181A] to-[#222227] border border-[#D4AF37]/40 text-center space-y-4">
          <h3 className="font-serif text-2xl sm:text-4xl font-bold text-white">
            Let's Craft Your Story Together
          </h3>
          <p className="text-xs sm:text-sm text-[#EBEBEB]/70 max-w-lg mx-auto font-light">
            We would be honored to accompany you on your day. Inquire to review our availability calendar.
          </p>
          <button
            onClick={() => navigateTo('booking')}
            className="px-8 py-3.5 rounded-xl bg-[#D4AF37] hover:bg-[#AA8C2C] text-black font-bold text-xs uppercase tracking-wider transition"
          >
            Check Event Availability
          </button>
        </div>
      </div>
    </div>
  );
};
