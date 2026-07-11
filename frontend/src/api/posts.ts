import { apiClient } from "./client";
import type { CursorPage, LikeAction, LikerSummary, Post, Visibility } from "./types";

export interface CreatePostPayload {
  content: string;
  visibility: Visibility;
  image?: File | null;
}

export async function createPost(payload: CreatePostPayload): Promise<Post> {
  const form = new FormData();
  form.set("content", payload.content);
  form.set("visibility", payload.visibility);
  if (payload.image) {
    form.set("image", payload.image);
  }
  const { data } = await apiClient.post<Post>("/api/posts", form);
  return data;
}

export async function fetchFeed(cursor: string | null, limit = 20): Promise<CursorPage<Post>> {
  const { data } = await apiClient.get<CursorPage<Post>>("/api/posts/feed", {
    params: { cursor: cursor ?? undefined, limit },
  });
  return data;
}

export async function fetchPost(postId: number): Promise<Post> {
  const { data } = await apiClient.get<Post>(`/api/posts/${postId}`);
  return data;
}

export async function deletePost(postId: number): Promise<void> {
  await apiClient.delete(`/api/posts/${postId}`);
}

export async function likePost(postId: number): Promise<LikeAction> {
  const { data } = await apiClient.post<LikeAction>(`/api/posts/${postId}/likes`);
  return data;
}

export async function unlikePost(postId: number): Promise<LikeAction> {
  const { data } = await apiClient.delete<LikeAction>(`/api/posts/${postId}/likes`);
  return data;
}

export async function fetchPostLikers(postId: number, cursor: string | null): Promise<CursorPage<LikerSummary>> {
  const { data } = await apiClient.get<CursorPage<LikerSummary>>(`/api/posts/${postId}/likes`, {
    params: { cursor: cursor ?? undefined },
  });
  return data;
}
