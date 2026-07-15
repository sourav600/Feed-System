import { HeartIcon } from "./icons";

interface LikeButtonProps {
  liked: boolean;
  pending: boolean;
  onToggle: () => void;
}

export function LikeButton({ liked, pending, onToggle }: LikeButtonProps) {
  return (
    <button
      type="button"
      className={`_feed_inner_timeline_reaction_emoji _feed_reaction${liked ? " _feed_reaction_active" : ""}`}
      onClick={onToggle}
      disabled={pending}
      aria-pressed={liked}
    >
      <span className="_feed_inner_timeline_reaction_link">
        <span>
          <HeartIcon filled={liked} />
          {liked ? "Liked" : "Like"}
        </span>
      </span>
    </button>
  );
}
