"use client";

import { useEffect, useState } from "react";

type RouteRecord = {
  id: number;
  originAddress: string;
  destinationAddress: string;
  distanceMeters?: number;
  durationSeconds?: number;
  createdAt?: string;
  current: boolean;
};

type RouteHistoryProps = {
  apiUrl: string;
  token: string;
  shipmentId: number;
};

function formatDistance(meters?: number) {
  return meters == null ? "Not available" : `${(meters / 1000).toFixed(1)} km`;
}

function formatDuration(seconds?: number) {
  return seconds == null ? "Not available" : `${Math.round(seconds / 60)} min`;
}

export default function RouteHistory({ apiUrl, token, shipmentId }: RouteHistoryProps) {
  const [routes, setRoutes] = useState<RouteRecord[]>([]);

  useEffect(() => {
    let active = true;
    fetch(`${apiUrl}/api/routes/${shipmentId}/history`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((response) => response.ok ? response.json() : [])
      .then((data: RouteRecord[]) => { if (active) setRoutes(data); })
      .catch(() => { if (active) setRoutes([]); });
    return () => { active = false; };
  }, [apiUrl, shipmentId, token]);

  if (routes.length === 0) return null;

  return (
    <div className="mt-4 border-t border-slate-800 pt-4">
      <h3 className="mb-3 text-sm font-semibold uppercase tracking-wider text-slate-400">Route history</h3>
      <div className="space-y-2">
        {routes.map((route) => (
          <div key={route.id} className={`rounded-lg border p-3 ${route.current ? "border-cyan-300/50 bg-cyan-300/10" : "border-slate-800 bg-slate-950/50"}`}>
            <div className="flex items-center justify-between gap-3">
              <p className="font-medium">{route.originAddress} to {route.destinationAddress}</p>
              {route.current && <span className="rounded-full bg-cyan-300 px-2 py-1 text-xs font-bold text-slate-950">Current</span>}
            </div>
            <p className="mt-1 text-xs text-slate-400">{formatDistance(route.distanceMeters)} · {formatDuration(route.durationSeconds)} · {route.createdAt ? new Date(route.createdAt).toLocaleString() : "Date unavailable"}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
