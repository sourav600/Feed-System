import { useState } from "react";
import type { Post } from "../../api/types";
import { fetchPostLikers } from "../../api/posts";
import { useLikePost, useUnlikePost } from "../../hooks/useFeed";
import { LikeButton } from "./LikeButton";
import { LikersModal } from "./LikersModal";
import { CommentThread } from "./CommentThread";

interface PostCardProps {
  post: Post;
}

const API_ORIGIN = (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? "http://localhost:8080";

function resolveImageUrl(url: string | null): string | null {
  if (!url) return null;
  return url.startsWith("http") ? url : `${API_ORIGIN}${url}`;
}

export function PostCard({ post }: PostCardProps) {
  const likeMutation = useLikePost();
  const unlikeMutation = useUnlikePost();
  const [showComments, setShowComments] = useState(false);
  const [showLikers, setShowLikers] = useState(false);

  const pending = likeMutation.isPending || unlikeMutation.isPending;
  const imageUrl = resolveImageUrl(post.imageUrl);

  return (
    <div className="_feed_inner_timeline_post_area _b_radious6 _padd_b24 _padd_t24 _mar_b16">
      <div className="_feed_inner_timeline_content _padd_r24 _padd_l24">
        <div className="_feed_inner_timeline_post_top">
          <div className="_feed_inner_timeline_post_box">
            <div className="_feed_inner_timeline_post_box_image">
              <img
                src={post.author.avatarUrl ?? "/assets/images/profile.png"}
                alt=""
                className="_post_img"
                width={44}
                height={44}
                style={{ borderRadius: "50%", objectFit: "cover" }}
              />
            </div>
            <div className="_feed_inner_timeline_post_box_txt">
              <h4 className="_feed_inner_timeline_post_box_title">
                {post.author.firstName} {post.author.lastName}
              </h4>
              <p className="_feed_inner_timeline_post_box_para">
                {formatRelativeTime(post.createdAt)}
                {" · "}
                <span title={post.visibility === "PRIVATE" ? "Only visible to you" : "Visible to everyone"}>
                  {post.visibility === "PRIVATE" ? "🔒 Private" : "🌐 Public"}
                </span>
              </p>
            </div>
          </div>
        </div>

        <p style={{ whiteSpace: "pre-wrap", margin: "12px 0" }}>{post.content}</p>

        {imageUrl && (
          <div className="_feed_inner_timeline_image" style={{ marginBottom: 16 }}>
            <img src={imageUrl} alt="" style={{ width: "100%", borderRadius: 8, objectFit: "cover" }} />
          </div>
        )}
      </div>

      <div className="_feed_inner_timeline_reaction" style={{ padding: "0 24px", display: "flex", gap: 20, alignItems: "center" }}>
        <LikeButton
          liked={post.likedByCurrentUser}
          likeCount={post.likeCount}
          pending={pending}
          onToggle={() => (post.likedByCurrentUser ? unlikeMutation.mutate(post.id) : likeMutation.mutate(post.id))}
          onShowLikers={() => setShowLikers(true)}
        />
        <button
          type="button"
          className="_feed_inner_timeline_reaction_comment _feed_reaction"
          onClick={() => setShowComments((v) => !v)}
        >
          <span className="_feed_inner_timeline_reaction_link">
            <span>
              💬 {post.commentCount} {post.commentCount === 1 ? "Comment" : "Comments"}
            </span>
          </span>
        </button>
      </div>

      {showComments && (
        <div style={{ padding: "0 24px", marginTop: 12 }}>
          <CommentThread postId={post.id} />
        </div>
      )}

      {showLikers && (
        <LikersModal
          title="Liked by"
          fetchPage={(cursor) => fetchPostLikers(post.id, cursor)}
          onClose={() => setShowLikers(false)}
        />
      )}
    </div>
  );
}

function formatRelativeTime(iso: string): string {
  const diffMs = Date.now() - new Date(iso).getTime();
  const minutes = Math.floor(diffMs / 60000);
  if (minutes < 1) return "just now";
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  if (days < 7) return `${days}d ago`;
  return new Date(iso).toLocaleDateString();
}
