interface LikeButtonProps {
  liked: boolean;
  likeCount: number;
  pending: boolean;
  onToggle: () => void;
  onShowLikers: () => void;
}

export function LikeButton({ liked, likeCount, pending, onToggle, onShowLikers }: LikeButtonProps) {
  return (
    <span style={{ display: "inline-flex", alignItems: "center", gap: 6 }}>
      <button
        type="button"
        className={`_feed_inner_timeline_reaction_emoji _feed_reaction${liked ? " _feed_reaction_active" : ""}`}
        onClick={onToggle}
        disabled={pending}
        aria-pressed={liked}
      >
        <span className="_feed_inner_timeline_reaction_link">
          <span>{liked ? "♥" : "♡"} Like</span>
        </span>
      </button>
      {likeCount > 0 && (
        <button
          type="button"
          onClick={onShowLikers}
          style={{ background: "none", border: "none", color: "#377DFF", cursor: "pointer", fontSize: 13 }}
        >
          {likeCount} {likeCount === 1 ? "like" : "likes"}
        </button>
      )}
    </span>
  );
}
