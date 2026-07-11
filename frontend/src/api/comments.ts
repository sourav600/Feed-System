import { apiClient } from "./client";
import type { Comment, CursorPage, LikeAction, LikerSummary } from "./types";

export async function fetchComments(postId: number, cursor: string | null): Promise<CursorPage<Comment>> {
  const { data } = await apiClient.get<CursorPage<Comment>>(`/api/posts/${postId}/comments`, {
    params: { cursor: cursor ?? undefined },
  });
  return data;
}

export async function createComment(postId: number, content: string): Promise<Comment> {
  const { data } = await apiClient.post<Comment>(`/api/posts/${postId}/comments`, { content });
  return data;
}

export async function fetchReplies(commentId: number, cursor: string | null): Promise<CursorPage<Comment>> {
  const { data } = await apiClient.get<CursorPage<Comment>>(`/api/comments/${commentId}/replies`, {
    params: { cursor: cursor ?? undefined },
  });
  return data;
}

export async function createReply(commentId: number, content: string): Promise<Comment> {
  const { data } = await apiClient.post<Comment>(`/api/comments/${commentId}/replies`, { content });
  return data;
}

export async function deleteComment(commentId: number): Promise<void> {
  await apiClient.delete(`/api/comments/${commentId}`);
}

export async function likeComment(commentId: number): Promise<LikeAction> {
  const { data } = await apiClient.post<LikeAction>(`/api/comments/${commentId}/likes`);
  return data;
}

export async function unlikeComment(commentId: number): Promise<LikeAction> {
  const { data } = await apiClient.delete<LikeAction>(`/api/comments/${commentId}/likes`);
  return data;
}

export async function fetchCommentLikers(commentId: number, cursor: string | null): Promise<CursorPage<LikerSummary>> {
  const { data } = await apiClient.get<CursorPage<LikerSummary>>(`/api/comments/${commentId}/likes`, {
    params: { cursor: cursor ?? undefined },
  });
  return data;
}
