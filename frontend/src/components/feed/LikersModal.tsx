import { useEffect, useState } from "react";
import type { CursorPage, LikerSummary } from "../../api/types";

interface LikersModalProps {
  title: string;
  fetchPage: (cursor: string | null) => Promise<CursorPage<LikerSummary>>;
  onClose: () => void;
}

export function LikersModal({ title, fetchPage, onClose }: LikersModalProps) {
  const [likers, setLikers] = useState<LikerSummary[]>([]);
  const [cursor, setCursor] = useState<string | null>(null);
  const [hasMore, setHasMore] = useState(false);
  const [loading, setLoading] = useState(true);

  async function loadPage(nextCursor: string | null) {
    setLoading(true);
    try {
      const page = await fetchPage(nextCursor);
      setLikers((prev) => (nextCursor ? [...prev, ...page.items] : page.items));
      setCursor(page.nextCursor);
      setHasMore(page.hasMore);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadPage(null);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div
      role="dialog"
      aria-modal="true"
      onClick={onClose}
      style={{
        position: "fixed",
        inset: 0,
        background: "rgba(17,32,50,0.5)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        zIndex: 1000,
      }}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        style={{
          background: "#fff",
          borderRadius: 12,
          width: 360,
          maxHeight: "70vh",
          overflowY: "auto",
          padding: 20,
        }}
      >
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 12 }}>
          <h4 style={{ margin: 0 }}>{title}</h4>
          <button type="button" onClick={onClose} style={{ background: "none", border: "none", fontSize: 18 }}>
            ×
          </button>
        </div>

        {likers.length === 0 && !loading && <p>No likes yet.</p>}

        <ul style={{ listStyle: "none", margin: 0, padding: 0 }}>
          {likers.map((liker) => (
            <li key={liker.userId} style={{ display: "flex", alignItems: "center", gap: 10, padding: "8px 0" }}>
              <img
                src={liker.avatarUrl ?? "/assets/images/Avatar.png"}
                alt=""
                width={36}
                height={36}
                style={{ borderRadius: "50%", objectFit: "cover" }}
              />
              <span>
                {liker.firstName} {liker.lastName}
              </span>
            </li>
          ))}
        </ul>

        {hasMore && (
          <button
            type="button"
            onClick={() => loadPage(cursor)}
            disabled={loading}
            style={{ marginTop: 8, background: "none", border: "none", color: "#377DFF", cursor: "pointer" }}
          >
            {loading ? "Loading..." : "Load more"}
          </button>
        )}
      </div>
    </div>
  );
}
