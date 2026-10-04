import React from 'react';
import { useApp } from '../context/AppContext';
import { Bell, Check, X, Calendar, DollarSign, MessageSquare, AlertCircle } from 'lucide-react';

interface NotificationModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const NotificationModal: React.FC<NotificationModalProps> = ({ isOpen, onClose }) => {
  const { notifications, currentUser, markNotificationRead, markAllNotificationsRead, navigateTo } = useApp();

  if (!isOpen) return null;

  const isAdmin = currentUser?.role === 'admin';
  const relevantNotifications = notifications.filter((n) => {
    if (isAdmin) return n.isAdminAlert;
    return n.recipientUserId === currentUser?.uid || (!n.isAdminAlert && !n.recipientUserId);
  });

  const getIcon = (type: string) => {
    switch (type) {
      case 'BOOKING_NEW':
      case 'STATUS_UPDATE':
        return <Calendar className="w-4 h-4 text-[#D4AF37]" />;
      case 'PAYMENT_RECEIVED':
        return <DollarSign className="w-4 h-4 text-emerald-400" />;
      case 'REVIEW_SUBMITTED':
      case 'CONTACT_MESSAGE':
        return <MessageSquare className="w-4 h-4 text-amber-400" />;
      default:
        return <Bell className="w-4 h-4 text-[#D4AF37]" />;
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-start justify-end p-4 sm:p-6 bg-black/60 backdrop-blur-sm animate-fadeIn">
      <div className="w-full max-w-md bg-[#18181B] border border-[#D4AF37]/30 rounded-2xl shadow-2xl shadow-black overflow-hidden flex flex-col max-h-[85vh]">
        {/* Header */}
        <div className="px-5 py-4 border-b border-white/10 flex items-center justify-between bg-[#121212]">
          <div className="flex items-center space-x-2.5">
            <div className="p-2 rounded-lg bg-[#D4AF37]/10 text-[#D4AF37]">
              <Bell className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-serif font-bold text-lg text-[#EBEBEB]">
                {isAdmin ? 'Admin Alerts' : 'Notifications'}
              </h3>
              <p className="text-xs text-[#EBEBEB]/60">
                {relevantNotifications.filter((n) => !n.read).length} unread updates
              </p>
            </div>
          </div>
          <div className="flex items-center space-x-1">
            {relevantNotifications.some((n) => !n.read) && (
              <button
                onClick={() => markAllNotificationsRead(isAdmin)}
                className="text-xs text-[#D4AF37] hover:underline px-2 py-1"
              >
                Mark all read
              </button>
            )}
            <button
              onClick={onClose}
              className="p-1.5 rounded-lg text-[#EBEBEB]/60 hover:text-white hover:bg-white/10"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Content list */}
        <div className="flex-1 overflow-y-auto p-4 space-y-3">
          {relevantNotifications.length === 0 ? (
            <div className="py-12 text-center text-[#EBEBEB]/50 space-y-2">
              <AlertCircle className="w-8 h-8 mx-auto text-[#EBEBEB]/30" />
              <p className="text-sm">No notifications found.</p>
            </div>
          ) : (
            relevantNotifications.map((notif) => (
              <div
                key={notif.id}
                onClick={() => {
                  if (!notif.read) markNotificationRead(notif.id);
                  if (isAdmin) {
                    navigateTo('admin');
                  } else if (notif.relatedBookingId) {
                    navigateTo('customer_area', { bookingId: notif.relatedBookingId });
                  }
                  onClose();
                }}
                className={`p-3.5 rounded-xl border transition cursor-pointer ${
                  notif.read
                    ? 'bg-white/[0.02] border-white/5 opacity-70 hover:opacity-100 hover:bg-white/5'
                    : 'bg-[#D4AF37]/5 border-[#D4AF37]/30 hover:bg-[#D4AF37]/10'
                }`}
              >
                <div className="flex items-start space-x-3">
                  <div className="p-2 rounded-lg bg-white/5 mt-0.5 shrink-0">
                    {getIcon(notif.type)}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between gap-2">
                      <h4 className="text-sm font-semibold text-[#EBEBEB] truncate">
                        {notif.title}
                      </h4>
                      <span className="text-[10px] text-[#EBEBEB]/40 shrink-0">
                        {new Date(notif.createdAt).toLocaleDateString([], {
                          month: 'short',
                          day: 'numeric',
                        })}
                      </span>
                    </div>
                    <p className="text-xs text-[#EBEBEB]/70 mt-1 leading-relaxed">
                      {notif.message}
                    </p>
                  </div>
                  {!notif.read && (
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        markNotificationRead(notif.id);
                      }}
                      className="p-1 text-[#D4AF37] hover:bg-[#D4AF37]/20 rounded"
                      title="Mark as read"
                    >
                      <Check className="w-3.5 h-3.5" />
                    </button>
                  )}
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
};
