import { useInfiniteQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  createComment,
  createReply,
  fetchComments,
  fetchReplies,
  likeComment,
  unlikeComment,
} from "../api/comments";
import type { Comment, CursorPage } from "../api/types";

export const commentsKey = (postId: number) => ["comments", postId] as const;
export const repliesKey = (commentId: number) => ["replies", commentId] as const;

export function useComments(postId: number, enabled: boolean) {
  return useInfiniteQuery({
    queryKey: commentsKey(postId),
    queryFn: ({ pageParam }) => fetchComments(postId, pageParam),
    initialPageParam: null as string | null,
    getNextPageParam: (lastPage) => (lastPage.hasMore ? lastPage.nextCursor : undefined),
    enabled,
  });
}

export function useReplies(commentId: number, enabled: boolean) {
  return useInfiniteQuery({
    queryKey: repliesKey(commentId),
    queryFn: ({ pageParam }) => fetchReplies(commentId, pageParam),
    initialPageParam: null as string | null,
    getNextPageParam: (lastPage) => (lastPage.hasMore ? lastPage.nextCursor : undefined),
    enabled,
  });
}

type InfinitePages = { pages: CursorPage<Comment>[]; pageParams: (string | null)[] };

export function useCreateComment(postId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (content: string) => createComment(postId, content),
    onSuccess: (comment) => {
      queryClient.setQueryData<InfinitePages>(commentsKey(postId), (existing) => {
        if (!existing) {
          return { pages: [{ items: [comment], nextCursor: null, hasMore: false }], pageParams: [null] };
        }
        const [firstPage, ...rest] = existing.pages;
        return { ...existing, pages: [{ ...firstPage, items: [comment, ...firstPage.items] }, ...rest] };
      });
    },
  });
}

export function useCreateReply(commentId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (content: string) => createReply(commentId, content),
    onSuccess: (reply) => {
      queryClient.setQueryData<InfinitePages>(repliesKey(commentId), (existing) => {
        if (!existing) {
          return { pages: [{ items: [reply], nextCursor: null, hasMore: false }], pageParams: [null] };
        }
        const [firstPage, ...rest] = existing.pages;
        return { ...existing, pages: [{ ...firstPage, items: [reply, ...firstPage.items] }, ...rest] };
      });
    },
  });
}

function patchCommentInCache(
  queryClient: ReturnType<typeof useQueryClient>,
  queryKey: readonly unknown[],
  commentId: number,
  liked: boolean,
  likeCount: number,
) {
  queryClient.setQueryData<InfinitePages>(queryKey, (existing) => {
    if (!existing) return existing;
    return {
      ...existing,
      pages: existing.pages.map((page) => ({
        ...page,
        items: page.items.map((c) => (c.id === commentId ? { ...c, likedByCurrentUser: liked, likeCount } : c)),
      })),
    };
  });
}

/** Works for both a top-level comment (lives in the post's comment page) and a reply (lives in its parent's reply page). */
export function useToggleCommentLike(postId: number, parentCommentId: number | null) {
  const queryClient = useQueryClient();
  const queryKey = parentCommentId ? repliesKey(parentCommentId) : commentsKey(postId);

  const like = useMutation({
    mutationFn: (commentId: number) => likeComment(commentId),
    onSuccess: (result, commentId) => patchCommentInCache(queryClient, queryKey, commentId, result.liked, result.likeCount),
  });
  const unlike = useMutation({
    mutationFn: (commentId: number) => unlikeComment(commentId),
    onSuccess: (result, commentId) => patchCommentInCache(queryClient, queryKey, commentId, result.liked, result.likeCount),
  });

  return { like, unlike };
}
