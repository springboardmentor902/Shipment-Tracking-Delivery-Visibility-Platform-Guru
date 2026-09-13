"use client";

import { useEffect, useState } from "react";
import RouteHistory from "./RouteHistory";

type Notification = {
  id: number;
  type: string;
  title: string;
  message: string;
  shipmentId?: number;
  readAt?: string;
  createdAt: string;
};

type PackageDraft = {
  description: string;
  quantity: number;
  weight: string;
};

type Shipment = {
  id: number;
  trackingNumber: string;
  status: string;
};

type RouteAnalytics = {
  averageDistanceMeters: number;
  timeEstimateAccuracyPercent: number;
  routeCount: number;
  bestRoute?: { originAddress: string; destinationAddress: string; distanceMeters?: number };
  worstRoute?: { originAddress: string; destinationAddress: string; distanceMeters?: number };
};

const apiUrl = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

function formatDate(value: string) {
  return new Intl.DateTimeFormat("en", { dateStyle: "medium", timeStyle: "short" }).format(new Date(value));
}

export default function Home() {
  const [token, setToken] = useState<string | null>(null);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loggingIn, setLoggingIn] = useState(false);
  const [trackingNumber, setTrackingNumber] = useState("");
  const [packageDraft, setPackageDraft] = useState<PackageDraft>({ description: "", quantity: 1, weight: "" });
  const [shipmentMessage, setShipmentMessage] = useState("");
  const [role, setRole] = useState("");
  const [shipments, setShipments] = useState<Shipment[]>([]);
  const [analytics, setAnalytics] = useState<RouteAnalytics | null>(null);

  async function loadNotifications() {
    const savedToken = window.localStorage.getItem("token");
    if (!savedToken) {
      setLoading(false);
      return;
    }
    try {
      setError("");
      setToken(savedToken);
      const response = await fetch(`${apiUrl}/api/notifications`, { headers: { Authorization: `Bearer ${savedToken}` } });
      if (!response.ok) throw new Error("Unable to load notifications.");
      setNotifications(await response.json());
      const savedRole = window.localStorage.getItem("role") ?? "";
      setRole(savedRole);
      const shipmentResponse = await fetch(`${apiUrl}/api/shipments`, { headers: { Authorization: `Bearer ${savedToken}` } });
      if (shipmentResponse.ok) setShipments(await shipmentResponse.json());
      if (savedRole === "ADMINISTRATOR") {
        const analyticsResponse = await fetch(`${apiUrl}/api/routes/analytics/summary`, { headers: { Authorization: `Bearer ${savedToken}` } });
        if (analyticsResponse.ok) setAnalytics(await analyticsResponse.json());
      }
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Unable to load notifications.");
    } finally {
      setLoading(false);
    }
  }

  async function login(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLoggingIn(true);
    setError("");
    try {
      const response = await fetch(`${apiUrl}/api/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });
      if (!response.ok) throw new Error("Invalid email or password.");
      const result = await response.json();
      window.localStorage.setItem("token", result.token);
      window.localStorage.setItem("role", result.user?.role ?? "");
      setToken(result.token);
      setRole(result.user?.role ?? "");
      setPassword("");
      await loadNotifications();
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Unable to log in.");
    } finally {
      setLoggingIn(false);
      setLoading(false);
    }
  }

  function logout() {
    window.localStorage.removeItem("token");
    window.localStorage.removeItem("role");
    setToken(null);
    setNotifications([]);
    setShipments([]);
    setAnalytics(null);
  }

  async function createShipment(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const savedToken = token ?? window.localStorage.getItem("token");
    if (!savedToken) return;
    setShipmentMessage("");
    const response = await fetch(`${apiUrl}/api/shipments`, {
      method: "POST",
      headers: { "Content-Type": "application/json", Authorization: `Bearer ${savedToken}` },
      body: JSON.stringify({ trackingNumber, packages: [{ ...packageDraft, weight: packageDraft.weight ? Number(packageDraft.weight) : null }] }),
    });
    if (!response.ok) {
      setShipmentMessage("Shipment creation is available to customers and business clients.");
      return;
    }
    setTrackingNumber("");
    setPackageDraft({ description: "", quantity: 1, weight: "" });
    setShipmentMessage("Shipment and package details saved.");
    await loadNotifications();
  }

  async function markAsRead(notification: Notification) {
    if (notification.readAt) return;
    const savedToken = token ?? window.localStorage.getItem("token");
    if (!savedToken) return;
    const response = await fetch(`${apiUrl}/api/notifications/${notification.id}/read`, {
      method: "PATCH",
      headers: { Authorization: `Bearer ${savedToken}` },
    });
    if (response.ok) {
      const updated = await response.json();
      setNotifications((current) => current.map((item) => item.id === updated.id ? updated : item));
    }
  }

  useEffect(() => { void loadNotifications(); }, []);

  const unreadCount = notifications.filter((notification) => !notification.readAt).length;

  return (
    <main className="min-h-screen bg-slate-950 px-5 py-8 text-slate-100 sm:px-10">
      <section className="mx-auto max-w-4xl">
        <header className="mb-8 flex items-center justify-between border-b border-slate-800 pb-6">
          <div>
            <p className="mb-2 text-sm font-semibold uppercase tracking-[0.2em] text-cyan-300">ShipTrack Pro</p>
            <h1 className="text-3xl font-semibold tracking-tight sm:text-4xl">Notification center</h1>
            <p className="mt-2 text-slate-400">Stay current on every shipment handoff and delay risk.</p>
          </div>
          {token && <div className="flex items-center gap-3"><button type="button" onClick={() => void loadNotifications()} aria-label="Refresh notifications" className="relative grid h-12 w-12 place-items-center rounded-full border border-slate-700 bg-slate-900 text-2xl transition hover:border-cyan-300 hover:text-cyan-300"><span aria-hidden="true">🔔</span>{unreadCount > 0 && <span className="absolute -right-1 -top-1 grid min-h-5 min-w-5 place-items-center rounded-full bg-cyan-300 px-1 text-xs font-bold text-slate-950">{unreadCount}</span>}</button><button type="button" onClick={logout} className="text-sm text-slate-400 hover:text-white">Log out</button></div>}
        </header>

        {error && <p className="rounded-lg border border-amber-400/30 bg-amber-400/10 p-4 text-amber-200">{error}</p>}
        {!token && !loading && <form onSubmit={login} className="mx-auto max-w-md space-y-4 rounded-xl border border-slate-800 bg-slate-900/70 p-6"><h2 className="text-xl font-semibold">Sign in</h2><input required type="email" value={email} onChange={(event) => setEmail(event.target.value)} placeholder="Email" className="w-full rounded-lg border border-slate-700 bg-slate-950 px-4 py-3 outline-none focus:border-cyan-300" /><input required type="password" value={password} onChange={(event) => setPassword(event.target.value)} placeholder="Password" className="w-full rounded-lg border border-slate-700 bg-slate-950 px-4 py-3 outline-none focus:border-cyan-300" /><button disabled={loggingIn} className="w-full rounded-lg bg-cyan-300 px-4 py-3 font-semibold text-slate-950 disabled:opacity-50">{loggingIn ? "Signing in..." : "Sign in"}</button></form>}
        {loading && <p className="text-slate-400">Loading notifications...</p>}
        {token && <form onSubmit={createShipment} className="mb-6 grid gap-3 rounded-xl border border-slate-800 bg-slate-900/70 p-5 sm:grid-cols-4"><h2 className="sm:col-span-4 text-lg font-semibold">Create shipment</h2><input required value={trackingNumber} onChange={(event) => setTrackingNumber(event.target.value)} placeholder="Tracking number" className="rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 outline-none focus:border-cyan-300" /><input required value={packageDraft.description} onChange={(event) => setPackageDraft({ ...packageDraft, description: event.target.value })} placeholder="Package description" className="rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 outline-none focus:border-cyan-300" /><input required min="1" type="number" value={packageDraft.quantity} onChange={(event) => setPackageDraft({ ...packageDraft, quantity: Number(event.target.value) })} placeholder="Quantity" className="rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 outline-none focus:border-cyan-300" /><input type="number" min="0" step="0.01" value={packageDraft.weight} onChange={(event) => setPackageDraft({ ...packageDraft, weight: event.target.value })} placeholder="Weight" className="rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 outline-none focus:border-cyan-300" /><button className="rounded-lg bg-cyan-300 px-4 py-2 font-semibold text-slate-950 sm:col-span-4">Save shipment and package</button>{shipmentMessage && <p className="text-sm text-slate-300 sm:col-span-4">{shipmentMessage}</p>}</form>}
        {token && shipments.length > 0 && <section className="mb-6 rounded-xl border border-slate-800 bg-slate-900/70 p-5"><h2 className="mb-4 text-lg font-semibold">Shipments</h2><div className="space-y-3">{shipments.map((shipment) => <div key={shipment.id} className="rounded-lg border border-slate-800 p-4"><div className="flex items-center justify-between"><div><p className="font-semibold">{shipment.trackingNumber}</p><p className="text-sm text-slate-400">{shipment.status}</p></div><span className="text-xs text-slate-500">#{shipment.id}</span></div><RouteHistory apiUrl={apiUrl} token={token} shipmentId={shipment.id} /></div>)}</div></section>}
        {role === "ADMINISTRATOR" && analytics && <section className="mb-6 rounded-xl border border-cyan-300/30 bg-cyan-300/10 p-5"><h2 className="text-lg font-semibold">Route analytics</h2><div className="mt-4 grid gap-3 sm:grid-cols-3"><div><p className="text-xs uppercase text-slate-400">Average distance</p><p className="text-xl font-semibold">{(analytics.averageDistanceMeters / 1000).toFixed(1)} km</p></div><div><p className="text-xs uppercase text-slate-400">Estimate accuracy</p><p className="text-xl font-semibold">{analytics.timeEstimateAccuracyPercent.toFixed(1)}%</p></div><div><p className="text-xs uppercase text-slate-400">Routes analyzed</p><p className="text-xl font-semibold">{analytics.routeCount}</p></div></div><div className="mt-4 grid gap-3 text-sm sm:grid-cols-2"><p>Best: {analytics.bestRoute?.originAddress ?? "None"} to {analytics.bestRoute?.destinationAddress ?? "None"}</p><p>Worst: {analytics.worstRoute?.originAddress ?? "None"} to {analytics.worstRoute?.destinationAddress ?? "None"}</p></div></section>}
        {!loading && !error && notifications.length === 0 && <div className="rounded-xl border border-dashed border-slate-700 p-10 text-center text-slate-400">You are all caught up.</div>}
        <div className="space-y-3">
          {notifications.map((notification) => (
            <button type="button" key={notification.id} onClick={() => void markAsRead(notification)} className={`w-full rounded-xl border p-5 text-left transition hover:border-cyan-300/70 ${notification.readAt ? "border-slate-800 bg-slate-900/50" : "border-cyan-300/40 bg-cyan-300/10"}`}>
              <div className="flex items-start justify-between gap-4">
                <div>
                  <div className="mb-2 flex items-center gap-2">
                    {!notification.readAt && <span className="h-2 w-2 rounded-full bg-cyan-300" aria-label="Unread" />}
                    <span className="text-xs font-semibold uppercase tracking-wider text-cyan-300">{notification.type.replace("_", " ")}</span>
                  </div>
                  <h2 className="text-lg font-semibold">{notification.title}</h2>
                  <p className="mt-1 text-slate-300">{notification.message}</p>
                </div>
                <time className="shrink-0 text-right text-xs text-slate-500">{formatDate(notification.createdAt)}</time>
              </div>
              {notification.shipmentId && <p className="mt-4 text-xs text-slate-500">Shipment #{notification.shipmentId}</p>}
            </button>
          ))}
        </div>
      </section>
    </main>
  );
}