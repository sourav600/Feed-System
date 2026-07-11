import { type FormEvent, useState } from "react";
import type { Comment } from "../../api/types";
import { fetchCommentLikers } from "../../api/comments";
import { useCreateReply, useReplies, useToggleCommentLike } from "../../hooks/useComments";
import { LikeButton } from "./LikeButton";
import { LikersModal } from "./LikersModal";

interface CommentItemProps {
  comment: Comment;
  postId: number;
}

export function CommentItem({ comment, postId }: CommentItemProps) {
  const isReply = comment.parentCommentId !== null;
  const { like, unlike } = useToggleCommentLike(postId, comment.parentCommentId);
  const [showLikers, setShowLikers] = useState(false);
  const [showReplies, setShowReplies] = useState(false);
  const [showReplyForm, setShowReplyForm] = useState(false);

  const pending = like.isPending || unlike.isPending;

  return (
    <div className="_comment_main" style={{ marginLeft: isReply ? 40 : 0 }}>
      <div className="_comment_image">
        <img
          src={comment.author.avatarUrl ?? "/assets/images/comment_img.png"}
          alt=""
          className="_comment_img1"
          width={36}
          height={36}
          style={{ borderRadius: "50%", objectFit: "cover" }}
        />
      </div>
      <div className="_comment_area">
        <div className="_comment_details">
          <div className="_comment_details_top">
            <div className="_comment_name">
              <h4 className="_comment_name_title">
                {comment.author.firstName} {comment.author.lastName}
              </h4>
            </div>
          </div>
          <div className="_comment_status">
            <p className="_comment_status_text">
              <span>{comment.content}</span>
            </p>
          </div>

          <div style={{ display: "flex", gap: 16, alignItems: "center", marginTop: 4 }}>
            <LikeButton
              liked={comment.likedByCurrentUser}
              likeCount={comment.likeCount}
              pending={pending}
              onToggle={() => (comment.likedByCurrentUser ? unlike.mutate(comment.id) : like.mutate(comment.id))}
              onShowLikers={() => setShowLikers(true)}
            />
            {!isReply && (
              <button
                type="button"
                onClick={() => setShowReplyForm((v) => !v)}
                style={{ background: "none", border: "none", color: "#666", cursor: "pointer", fontSize: 13 }}
              >
                Reply
              </button>
            )}
          </div>

          {showReplyForm && !isReply && (
            <ReplyForm commentId={comment.id} onDone={() => { setShowReplyForm(false); setShowReplies(true); }} />
          )}

          {!isReply && comment.replyCount > 0 && (
            <button
              type="button"
              onClick={() => setShowReplies((v) => !v)}
              style={{ background: "none", border: "none", color: "#377DFF", cursor: "pointer", fontSize: 13, marginTop: 6 }}
            >
              {showReplies ? "Hide replies" : `View ${comment.replyCount} ${comment.replyCount === 1 ? "reply" : "replies"}`}
            </button>
          )}

          {!isReply && showReplies && <RepliesList postId={postId} commentId={comment.id} />}
        </div>
      </div>

      {showLikers && (
        <LikersModal
          title="Liked by"
          fetchPage={(cursor) => fetchCommentLikers(comment.id, cursor)}
          onClose={() => setShowLikers(false)}
        />
      )}
    </div>
  );
}

function ReplyForm({ commentId, onDone }: { commentId: number; onDone: () => void }) {
  const [content, setContent] = useState("");
  const createReply = useCreateReply(commentId);

  function handleSubmit(event: FormEvent) {
    event.preventDefault();
    if (!content.trim()) return;
    createReply.mutate(content, {
      onSuccess: () => {
        setContent("");
        onDone();
      },
    });
  }

  return (
    <form onSubmit={handleSubmit} style={{ display: "flex", gap: 8, marginTop: 8 }}>
      <input
        type="text"
        value={content}
        onChange={(e) => setContent(e.target.value)}
        placeholder="Write a reply"
        className="form-control _comment_textarea"
        style={{ flex: 1 }}
      />
      <button type="submit" className="_btn1" disabled={createReply.isPending} style={{ padding: "4px 14px" }}>
        Reply
      </button>
    </form>
  );
}

function RepliesList({ postId, commentId }: { postId: number; commentId: number }) {
  const { data, isLoading, fetchNextPage, hasNextPage, isFetchingNextPage } = useReplies(commentId, true);
  const replies = data?.pages.flatMap((page) => page.items) ?? [];

  if (isLoading) return <p style={{ fontSize: 13, color: "#666" }}>Loading replies...</p>;

  return (
    <div style={{ marginTop: 10 }}>
      {replies.map((reply) => (
        <CommentItem key={reply.id} comment={reply} postId={postId} />
      ))}
      {hasNextPage && (
        <button
          type="button"
          onClick={() => fetchNextPage()}
          disabled={isFetchingNextPage}
          style={{ background: "none", border: "none", color: "#377DFF", cursor: "pointer", fontSize: 13 }}
        >
          {isFetchingNextPage ? "Loading..." : "Load more replies"}
        </button>
      )}
    </div>
  );
}
