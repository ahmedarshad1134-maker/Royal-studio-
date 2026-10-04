import React, { createContext, useContext, useState, useEffect } from 'react';
import {
  ScreenId,
  User,
  ServiceItem,
  PackageItem,
  PortfolioItem,
  ReviewItem,
  Booking,
  BookingStatus,
  BlockedDate,
  Invoice,
  PaymentRecord,
  StudioSettings,
  AppNotification,
  ContactMessage,
  LineItem,
} from '../types';
import {
  INITIAL_SERVICES,
  INITIAL_PACKAGES,
  INITIAL_PORTFOLIO,
  INITIAL_REVIEWS,
  INITIAL_SETTINGS,
  INITIAL_BLOCKED_DATES,
  INITIAL_BOOKINGS,
  INITIAL_INVOICES,
  INITIAL_NOTIFICATIONS,
  DEMO_USERS,
} from '../data/initialData';

interface NavigationHistoryItem {
  screen: ScreenId;
  params?: { serviceId?: string; packageId?: string; bookingId?: string };
}

interface AppContextType {
  // Navigation
  currentScreen: ScreenId;
  screenParams: { serviceId?: string; packageId?: string; bookingId?: string };
  navigateTo: (screen: ScreenId, params?: { serviceId?: string; packageId?: string; bookingId?: string }) => void;
  goBack: () => void;
  
  // Auth
  currentUser: User | null;
  login: (email: string, role: 'customer' | 'admin', name?: string, phone?: string) => Promise<boolean>;
  logout: () => void;
  demoUsers: User[];
  
  // Services & Packages
  services: ServiceItem[];
  packages: PackageItem[];
  getServiceById: (id: string) => ServiceItem | undefined;
  getPackageById: (id: string) => PackageItem | undefined;
  updateService: (service: ServiceItem) => void;
  addService: (service: Omit<ServiceItem, 'id'>) => void;
  updatePackage: (pkg: PackageItem) => void;
  addPackage: (pkg: Omit<PackageItem, 'id'>) => void;
  
  // Portfolio
  portfolio: PortfolioItem[];
  addPortfolioItem: (item: Omit<PortfolioItem, 'id' | 'createdAt'>) => void;
  updatePortfolioItem: (item: PortfolioItem) => void;
  deletePortfolioItem: (id: string) => void;
  
  // Bookings
  bookings: Booking[];
  getBookingById: (id: string) => Booking | undefined;
  createBooking: (data: {
    customerName: string;
    phone: string;
    email: string;
    eventType: string;
    eventDate: number;
    eventLocation: string;
    serviceId?: string;
    packageId?: string;
    message?: string;
  }) => Booking;
  updateBookingStatus: (bookingId: string, status: BookingStatus, note?: string) => void;
  updateBookingNotes: (bookingId: string, adminNotes: string) => void;
  
  // Blocked Dates
  blockedDates: BlockedDate[];
  addBlockedDate: (date: number, reason: string) => void;
  removeBlockedDate: (id: string) => void;
  isDateBlocked: (dateMillis: number) => boolean;
  
  // Reviews
  reviews: ReviewItem[];
  submitReview: (name: string, rating: number, eventType: string, reviewText: string) => Promise<void>;
  moderateReview: (reviewId: string, approved: boolean, featured?: boolean) => void;
  deleteReview: (reviewId: string) => void;
  
  // Invoices
  invoices: Invoice[];
  getInvoiceByBookingId: (bookingId: string) => Invoice | undefined;
  createInvoiceForBooking: (booking: Booking, customTotal?: number) => Invoice;
  updateInvoice: (invoice: Invoice) => void;
  recordPayment: (
    invoiceId: string,
    amount: number,
    method: 'Bank Transfer' | 'Cash' | 'Card' | 'UPI' | 'Cheque',
    referenceNotes: string
  ) => void;
  
  // Studio Settings & Messages
  settings: StudioSettings;
  updateSettings: (newSettings: Partial<StudioSettings>) => void;
  contactMessages: ContactMessage[];
  submitContactMessage: (name: string, phone: string, email: string, message: string) => Promise<void>;
  
  // Notifications
  notifications: AppNotification[];
  markNotificationRead: (id: string) => void;
  markAllNotificationsRead: (isAdmin: boolean) => void;
}

const AppContext = createContext<AppContextType | null>(null);

function loadFromStorage<T>(key: string, defaultValue: T): T {
  try {
    const item = localStorage.getItem(`royal_studio_${key}`);
    return item ? JSON.parse(item) : defaultValue;
  } catch (e) {
    console.warn(`Failed reading storage for ${key}`, e);
    return defaultValue;
  }
}

function saveToStorage<T>(key: string, value: T): void {
  try {
    localStorage.setItem(`royal_studio_${key}`, JSON.stringify(value));
  } catch (e) {
    console.warn(`Failed writing storage for ${key}`, e);
  }
}

export const AppProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  // Navigation State
  const [currentScreen, setCurrentScreen] = useState<ScreenId>('home');
  const [screenParams, setScreenParams] = useState<{ serviceId?: string; packageId?: string; bookingId?: string }>({});
  const [navHistory, setNavHistory] = useState<NavigationHistoryItem[]>([{ screen: 'home' }]);

  // Authentication State
  const [currentUser, setCurrentUser] = useState<User | null>(() =>
    loadFromStorage<User | null>('currentUser', DEMO_USERS[1]) // Default to demo customer Sarah for instant richness
  );

  // Entities State
  const [services, setServices] = useState<ServiceItem[]>(() =>
    loadFromStorage('services', INITIAL_SERVICES)
  );
  const [packages, setPackages] = useState<PackageItem[]>(() =>
    loadFromStorage('packages', INITIAL_PACKAGES)
  );
  const [portfolio, setPortfolio] = useState<PortfolioItem[]>(() =>
    loadFromStorage('portfolio', INITIAL_PORTFOLIO)
  );
  const [reviews, setReviews] = useState<ReviewItem[]>(() =>
    loadFromStorage('reviews', INITIAL_REVIEWS)
  );
  const [bookings, setBookings] = useState<Booking[]>(() =>
    loadFromStorage('bookings', INITIAL_BOOKINGS)
  );
  const [blockedDates, setBlockedDates] = useState<BlockedDate[]>(() =>
    loadFromStorage('blockedDates', INITIAL_BLOCKED_DATES)
  );
  const [invoices, setInvoices] = useState<Invoice[]>(() =>
    loadFromStorage('invoices', INITIAL_INVOICES)
  );
  const [settings, setSettings] = useState<StudioSettings>(() =>
    loadFromStorage('settings', INITIAL_SETTINGS)
  );
  const [notifications, setNotifications] = useState<AppNotification[]>(() =>
    loadFromStorage('notifications', INITIAL_NOTIFICATIONS)
  );
  const [contactMessages, setContactMessages] = useState<ContactMessage[]>(() =>
    loadFromStorage('contactMessages', [])
  );

  // Save changes to localStorage
  useEffect(() => saveToStorage('currentUser', currentUser), [currentUser]);
  useEffect(() => saveToStorage('services', services), [services]);
  useEffect(() => saveToStorage('packages', packages), [packages]);
  useEffect(() => saveToStorage('portfolio', portfolio), [portfolio]);
  useEffect(() => saveToStorage('reviews', reviews), [reviews]);
  useEffect(() => saveToStorage('bookings', bookings), [bookings]);
  useEffect(() => saveToStorage('blockedDates', blockedDates), [blockedDates]);
  useEffect(() => saveToStorage('invoices', invoices), [invoices]);
  useEffect(() => saveToStorage('settings', settings), [settings]);
  useEffect(() => saveToStorage('notifications', notifications), [notifications]);
  useEffect(() => saveToStorage('contactMessages', contactMessages), [contactMessages]);

  // Navigation handlers
  const navigateTo = (screen: ScreenId, params?: { serviceId?: string; packageId?: string; bookingId?: string }) => {
    setScreenParams(params || {});
    setCurrentScreen(screen);
    setNavHistory((prev) => [...prev, { screen, params }]);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const goBack = () => {
    if (navHistory.length > 1) {
      const newHistory = [...navHistory];
      newHistory.pop();
      const prevItem = newHistory[newHistory.length - 1];
      setNavHistory(newHistory);
      setCurrentScreen(prevItem.screen);
      setScreenParams(prevItem.params || {});
    } else {
      setCurrentScreen('home');
      setScreenParams({});
    }
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  // Auth functions
  const login = async (email: string, role: 'customer' | 'admin', name?: string, phone?: string): Promise<boolean> => {
    // If demo user exists, use matching profile
    const existing = DEMO_USERS.find(
      (u) => u.email.toLowerCase() === email.toLowerCase() && u.role === role
    );
    if (existing) {
      setCurrentUser(existing);
      return true;
    }
    const newUser: User = {
      uid: `usr_${Date.now()}`,
      name: name || (email.split('@')[0] || 'User'),
      email,
      phone: phone || '+1 555-000-0000',
      role,
      createdAt: new Date().toISOString(),
    };
    setCurrentUser(newUser);
    return true;
  };

  const logout = () => {
    setCurrentUser(null);
  };

  // Services & Packages getters and modifiers
  const getServiceById = (id: string) => services.find((s) => s.id === id);
  const getPackageById = (id: string) => packages.find((p) => p.id === id);

  const updateService = (service: ServiceItem) => {
    setServices((prev) => prev.map((s) => (s.id === service.id ? service : s)));
  };

  const addService = (service: Omit<ServiceItem, 'id'>) => {
    const id = `svc_${Date.now()}`;
    setServices((prev) => [...prev, { ...service, id }]);
  };

  const updatePackage = (pkg: PackageItem) => {
    setPackages((prev) => prev.map((p) => (p.id === pkg.id ? pkg : p)));
  };

  const addPackage = (pkg: Omit<PackageItem, 'id'>) => {
    const id = `pkg_${Date.now()}`;
    setPackages((prev) => [...prev, { ...pkg, id }]);
  };

  // Portfolio CRUD
  const addPortfolioItem = (item: Omit<PortfolioItem, 'id' | 'createdAt'>) => {
    const newItem: PortfolioItem = {
      ...item,
      id: `port_${Date.now()}`,
      createdAt: Date.now(),
    };
    setPortfolio((prev) => [newItem, ...prev]);
  };

  const updatePortfolioItem = (item: PortfolioItem) => {
    setPortfolio((prev) => prev.map((p) => (p.id === item.id ? item : p)));
  };

  const deletePortfolioItem = (id: string) => {
    setPortfolio((prev) => prev.filter((p) => p.id !== id));
  };

  // Bookings CRUD
  const getBookingById = (id: string) =>
    bookings.find((b) => b.id === id || b.referenceId === id);

  const createBooking = (data: {
    customerName: string;
    phone: string;
    email: string;
    eventType: string;
    eventDate: number;
    eventLocation: string;
    serviceId?: string;
    packageId?: string;
    message?: string;
  }): Booking => {
    const referenceNum = Math.floor(1000 + Math.random() * 9000);
    const referenceId = `ROYAL-2026-${referenceNum}`;
    const id = `book_${Date.now()}`;

    const newBooking: Booking = {
      id,
      referenceId,
      customerId: currentUser?.uid || `guest_${Date.now()}`,
      customerName: data.customerName,
      phone: data.phone,
      email: data.email,
      eventType: data.eventType,
      eventDate: data.eventDate,
      eventLocation: data.eventLocation,
      serviceId: data.serviceId,
      packageId: data.packageId,
      message: data.message,
      status: 'NEW',
      createdAt: Date.now(),
      auditTrail: [
        {
          status: 'NEW',
          updatedBy: `${data.customerName} (Client)`,
          timestamp: Date.now(),
          note: `Booking enquiry registered online for ${data.eventType} on ${new Date(data.eventDate).toLocaleDateString()}.`,
        },
      ],
    };

    setBookings((prev) => [newBooking, ...prev]);

    // Send admin notification
    const newNotif: AppNotification = {
      id: `notif_${Date.now()}`,
      isAdminAlert: true,
      title: 'New Booking Enquiry Received',
      message: `${data.customerName} has enquired for ${data.eventType} on ${new Date(data.eventDate).toLocaleDateString()} (#${referenceId}).`,
      type: 'BOOKING_NEW',
      relatedBookingId: id,
      read: false,
      createdAt: Date.now(),
    };
    setNotifications((prev) => [newNotif, ...prev]);

    return newBooking;
  };

  const updateBookingStatus = (bookingId: string, status: BookingStatus, note?: string) => {
    setBookings((prev) =>
      prev.map((b) => {
        if (b.id !== bookingId) return b;
        const newAudit: { status: string; updatedBy: string; timestamp: number; note: string } = {
          status,
          updatedBy: currentUser?.name || 'Admin Producer',
          timestamp: Date.now(),
          note: note || `Status updated to ${status}.`,
        };
        return {
          ...b,
          status,
          updatedAt: Date.now(),
          updatedBy: currentUser?.name || 'Admin',
          auditTrail: [...b.auditTrail, newAudit],
        };
      })
    );

    // Notify customer
    const booking = bookings.find((b) => b.id === bookingId);
    if (booking) {
      const notif: AppNotification = {
        id: `notif_${Date.now()}`,
        recipientUserId: booking.customerId,
        isAdminAlert: false,
        title: `Booking Status: ${status}`,
        message: `Your booking #${booking.referenceId} has been updated to "${status}".`,
        type: 'STATUS_UPDATE',
        relatedBookingId: booking.id,
        read: false,
        createdAt: Date.now(),
      };
      setNotifications((prev) => [notif, ...prev]);
    }
  };

  const updateBookingNotes = (bookingId: string, adminNotes: string) => {
    setBookings((prev) =>
      prev.map((b) => (b.id === bookingId ? { ...b, adminNotes, updatedAt: Date.now() } : b))
    );
  };

  // Blocked Dates
  const isDateBlocked = (dateMillis: number): boolean => {
    if (blockedDates.length === 0) return false;
    const target = new Date(dateMillis);
    const targetY = target.getFullYear();
    const targetM = target.getMonth();
    const targetD = target.getDate();

    return blockedDates.some((b) => {
      const bDate = new Date(b.date);
      return (
        bDate.getFullYear() === targetY &&
        bDate.getMonth() === targetM &&
        bDate.getDate() === targetD
      );
    });
  };

  const addBlockedDate = (date: number, reason: string) => {
    const newBlocked: BlockedDate = {
      id: `block_${Date.now()}`,
      date,
      reason,
      createdAt: Date.now(),
    };
    setBlockedDates((prev) => [...prev, newBlocked]);
  };

  const removeBlockedDate = (id: string) => {
    setBlockedDates((prev) => prev.filter((b) => b.id !== id));
  };

  // Reviews
  const submitReview = async (name: string, rating: number, eventType: string, reviewText: string) => {
    const reviewId = `REV-${Date.now().toString().slice(-6)}`;
    const newReview: ReviewItem = {
      id: reviewId,
      customerId: currentUser?.uid,
      customerName: name,
      rating,
      eventType,
      review: reviewText,
      approved: false, // Moderation workflow
      featured: false,
      createdAt: Date.now(),
    };
    setReviews((prev) => [newReview, ...prev]);

    // Admin alert
    const notif: AppNotification = {
      id: `notif_${Date.now()}`,
      isAdminAlert: true,
      title: 'New Review Awaiting Moderation',
      message: `${name} submitted a ${rating}-star review for ${eventType}.`,
      type: 'REVIEW_SUBMITTED',
      read: false,
      createdAt: Date.now(),
    };
    setNotifications((prev) => [notif, ...prev]);
  };

  const moderateReview = (reviewId: string, approved: boolean, featured?: boolean) => {
    setReviews((prev) =>
      prev.map((r) =>
        r.id === reviewId
          ? {
              ...r,
              approved,
              featured: featured !== undefined ? featured : r.featured,
            }
          : r
      )
    );
  };

  const deleteReview = (reviewId: string) => {
    setReviews((prev) => prev.filter((r) => r.id !== reviewId));
  };

  // Invoices
  const getInvoiceByBookingId = (bookingId: string) =>
    invoices.find((i) => i.bookingId === bookingId || i.bookingReference === bookingId);

  const createInvoiceForBooking = (booking: Booking, customTotal?: number): Invoice => {
    const pkg = packages.find((p) => p.id === booking.packageId);
    const svc = services.find((s) => s.id === booking.serviceId);

    const priceNum = customTotal || (pkg ? parseFloat(pkg.price.replace(/[^0-9.]/g, '')) || 2500 : 2500);

    const lineItems: LineItem[] = [
      {
        description: pkg ? `${pkg.name} Coverage` : svc ? svc.name : 'Event Visual Production',
        amount: priceNum,
        category: 'PACKAGE',
      },
    ];

    const invoiceNumber = `INV-${new Date().getFullYear()}-${booking.referenceId.slice(-4)}`;
    const newInvoice: Invoice = {
      id: `inv_${Date.now()}`,
      invoiceNumber,
      bookingId: booking.id,
      bookingReference: booking.referenceId,
      customerId: booking.customerId,
      customerName: booking.customerName,
      customerPhone: booking.phone,
      customerEmail: booking.email,
      eventType: booking.eventType,
      eventDate: booking.eventDate,
      eventLocation: booking.eventLocation,
      selectedPackage: pkg ? pkg.name : 'Custom Selection',
      selectedServices: svc ? [svc.name] : ['Visual Media'],
      lineItems,
      subtotal: priceNum,
      additionalCharges: 0,
      discount: 0,
      taxRate: 0,
      taxAmount: 0,
      totalAmount: priceNum,
      amountPaid: 0,
      balanceDue: priceNum,
      paymentStatus: 'Unpaid',
      currency: 'USD',
      issueDate: Date.now(),
      dueDate: booking.eventDate - 86400000 * 3, // Due 3 days before event
      paymentRecords: [],
      notes: 'Payment required prior to event commencement.',
      createdAt: Date.now(),
    };

    setInvoices((prev) => [newInvoice, ...prev]);
    return newInvoice;
  };

  const updateInvoice = (updated: Invoice) => {
    setInvoices((prev) => prev.map((i) => (i.id === updated.id ? updated : i)));
  };

  const recordPayment = (
    invoiceId: string,
    amount: number,
    method: 'Bank Transfer' | 'Cash' | 'Card' | 'UPI' | 'Cheque',
    referenceNotes: string
  ) => {
    setInvoices((prev) =>
      prev.map((inv) => {
        if (inv.id !== invoiceId) return inv;

        const newPaid = inv.amountPaid + amount;
        const newBalance = Math.max(0, inv.totalAmount - newPaid);
        const newStatus = newBalance <= 0 ? 'Paid' : newPaid > 0 ? 'Partially Paid' : 'Unpaid';

        const paymentRecord: PaymentRecord = {
          paymentId: `PAY-${Date.now().toString().slice(-4)}`,
          amount,
          method,
          referenceNotes,
          recordedBy: currentUser?.name || 'Studio Administrator',
          timestamp: Date.now(),
          receiptId: `REC-${new Date().getFullYear()}-${Math.floor(100 + Math.random() * 900)}`,
        };

        const updatedInvoice: Invoice = {
          ...inv,
          amountPaid: newPaid,
          balanceDue: newBalance,
          paymentStatus: newStatus,
          paymentRecords: [...inv.paymentRecords, paymentRecord],
          updatedAt: Date.now(),
        };

        // Notify client
        const notif: AppNotification = {
          id: `notif_${Date.now()}`,
          recipientUserId: inv.customerId,
          isAdminAlert: false,
          title: 'Payment Received & Receipt Generated',
          message: `Received $${amount.toLocaleString()} for Invoice ${inv.invoiceNumber}. Remaining balance: $${newBalance.toLocaleString()}.`,
          type: 'PAYMENT_RECEIVED',
          relatedBookingId: inv.bookingId,
          read: false,
          createdAt: Date.now(),
        };
        setNotifications((p) => [notif, ...p]);

        return updatedInvoice;
      })
    );
  };

  // Settings & Contact Messages
  const updateSettings = (newSettings: Partial<StudioSettings>) => {
    setSettings((prev) => ({ ...prev, ...newSettings }));
  };

  const submitContactMessage = async (name: string, phone: string, email: string, message: string) => {
    const newMsg: ContactMessage = {
      id: `msg_${Date.now()}`,
      name,
      phone,
      email,
      message,
      createdAt: Date.now(),
    };
    setContactMessages((prev) => [newMsg, ...prev]);

    // Admin alert
    const notif: AppNotification = {
      id: `notif_${Date.now()}`,
      isAdminAlert: true,
      title: 'New Contact Inquiry Received',
      message: `${name} (${phone}) sent a message: "${message.slice(0, 80)}${message.length > 80 ? '...' : ''}"`,
      type: 'CONTACT_MESSAGE',
      read: false,
      createdAt: Date.now(),
    };
    setNotifications((prev) => [notif, ...prev]);
  };

  // Notifications
  const markNotificationRead = (id: string) => {
    setNotifications((prev) => prev.map((n) => (n.id === id ? { ...n, read: true } : n)));
  };

  const markAllNotificationsRead = (isAdmin: boolean) => {
    setNotifications((prev) =>
      prev.map((n) => {
        if (isAdmin && n.isAdminAlert) return { ...n, read: true };
        if (!isAdmin && n.recipientUserId === currentUser?.uid) return { ...n, read: true };
        return n;
      })
    );
  };

  return (
    <AppContext.Provider
      value={{
        currentScreen,
        screenParams,
        navigateTo,
        goBack,
        currentUser,
        login,
        logout,
        demoUsers: DEMO_USERS,
        services,
        packages,
        getServiceById,
        getPackageById,
        updateService,
        addService,
        updatePackage,
        addPackage,
        portfolio,
        addPortfolioItem,
        updatePortfolioItem,
        deletePortfolioItem,
        bookings,
        getBookingById,
        createBooking,
        updateBookingStatus,
        updateBookingNotes,
        blockedDates,
        addBlockedDate,
        removeBlockedDate,
        isDateBlocked,
        reviews,
        submitReview,
        moderateReview,
        deleteReview,
        invoices,
        getInvoiceByBookingId,
        createInvoiceForBooking,
        updateInvoice,
        recordPayment,
        settings,
        updateSettings,
        contactMessages,
        submitContactMessage,
        notifications,
        markNotificationRead,
        markAllNotificationsRead,
      }}
    >
      {children}
    </AppContext.Provider>
  );
};

export const useApp = () => {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error('useApp must be used within an AppProvider');
  }
  return context;
};
