import { type FormEvent, useState } from "react";
import { useComments, useCreateComment } from "../../hooks/useComments";
import { CommentItem } from "./CommentItem";

interface CommentThreadProps {
  postId: number;
}

export function CommentThread({ postId }: CommentThreadProps) {
  const { data, isLoading, fetchNextPage, hasNextPage, isFetchingNextPage } = useComments(postId, true);
  const createComment = useCreateComment(postId);
  const [content, setContent] = useState("");

  const comments = data?.pages.flatMap((page) => page.items) ?? [];

  function handleSubmit(event: FormEvent) {
    event.preventDefault();
    if (!content.trim()) return;
    createComment.mutate(content, { onSuccess: () => setContent("") });
  }

  return (
    <div className="_feed_inner_timeline_cooment_area">
      <div className="_feed_inner_comment_box">
        <form className="_feed_inner_comment_box_form" onSubmit={handleSubmit}>
          <div className="_feed_inner_comment_box_content">
            <div className="_feed_inner_comment_box_content_txt">
              <textarea
                className="form-control _comment_textarea"
                placeholder="Write a comment"
                value={content}
                onChange={(e) => setContent(e.target.value)}
                rows={1}
              />
            </div>
          </div>
          <button type="submit" className="_btn1" disabled={createComment.isPending} style={{ marginTop: 6, padding: "4px 14px" }}>
            Post
          </button>
        </form>
      </div>

      <div className="_timline_comment_main">
        {isLoading && <p style={{ fontSize: 13, color: "#666" }}>Loading comments...</p>}
        {comments.map((comment) => (
          <CommentItem key={comment.id} comment={comment} postId={postId} />
        ))}
        {hasNextPage && (
          <button
            type="button"
            className="_previous_comment_txt"
            onClick={() => fetchNextPage()}
            disabled={isFetchingNextPage}
          >
            {isFetchingNextPage ? "Loading..." : "View more comments"}
          </button>
        )}
      </div>
    </div>
  );
}
