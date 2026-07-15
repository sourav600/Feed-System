import { useMutation, useQueryClient, useInfiniteQuery } from "@tanstack/react-query";
import {
  createPost,
  deletePost,
  fetchFeed,
  likePost,
  unlikePost,
  type CreatePostPayload,
} from "../api/posts";
import type { CursorPage, Post } from "../api/types";

export const FEED_KEY = ["feed"] as const;

export function useFeed() {
  return useInfiniteQuery({
    queryKey: FEED_KEY,
    queryFn: ({ pageParam }) => fetchFeed(pageParam),
    initialPageParam: null as string | null,
    getNextPageParam: (lastPage) => (lastPage.hasMore ? lastPage.nextCursor : undefined),
  });
}

export function useCreatePost() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: CreatePostPayload) => createPost(payload),
    onSuccess: (post) => {
      // Prepend the new post to the first page rather than refetching the whole feed - the
      // newest-first ordering guarantees it belongs at the very top.
      queryClient.setQueryData<{ pages: CursorPage<Post>[]; pageParams: (string | null)[] }>(
        FEED_KEY,
        (existing) => {
          if (!existing) return existing;
          const [firstPage, ...restPages] = existing.pages;
          return {
            ...existing,
            pages: [{ ...firstPage, items: [post, ...firstPage.items] }, ...restPages],
          };
        },
      );
    },
  });
}

export function useDeletePost() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (postId: number) => deletePost(postId),
    onSuccess: (_result, postId) => {
      queryClient.setQueryData<{ pages: CursorPage<Post>[]; pageParams: (string | null)[] }>(
        FEED_KEY,
        (existing) => {
          if (!existing) return existing;
          return {
            ...existing,
            pages: existing.pages.map((page) => ({
              ...page,
              items: page.items.filter((post) => post.id !== postId),
            })),
          };
        },
      );
    },
  });
}

function toggleLikeInCache(
  queryClient: ReturnType<typeof useQueryClient>,
  postId: number,
  liked: boolean,
  likeCount: number,
) {
  queryClient.setQueryData<{ pages: CursorPage<Post>[]; pageParams: (string | null)[] }>(
    FEED_KEY,
    (existing) => {
      if (!existing) return existing;
      return {
        ...existing,
        pages: existing.pages.map((page) => ({
          ...page,
          items: page.items.map((post) =>
            post.id === postId ? { ...post, likedByCurrentUser: liked, likeCount } : post,
          ),
        })),
      };
    },
  );
}

export function useLikePost() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (postId: number) => likePost(postId),
    onMutate: async (postId) => {
      await queryClient.cancelQueries({ queryKey: FEED_KEY });
      const previous = queryClient.getQueryData(FEED_KEY);
      toggleLikeInCache(queryClient, postId, true, findLikeCount(queryClient, postId) + 1);
      return { previous };
    },
    onError: (_err, _postId, context) => {
      if (context?.previous) queryClient.setQueryData(FEED_KEY, context.previous);
    },
    onSuccess: (result, postId) => toggleLikeInCache(queryClient, postId, result.liked, result.likeCount),
  });
}

export function useUnlikePost() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (postId: number) => unlikePost(postId),
    onMutate: async (postId) => {
      await queryClient.cancelQueries({ queryKey: FEED_KEY });
      const previous = queryClient.getQueryData(FEED_KEY);
      toggleLikeInCache(queryClient, postId, false, Math.max(0, findLikeCount(queryClient, postId) - 1));
      return { previous };
    },
    onError: (_err, _postId, context) => {
      if (context?.previous) queryClient.setQueryData(FEED_KEY, context.previous);
    },
    onSuccess: (result, postId) => toggleLikeInCache(queryClient, postId, result.liked, result.likeCount),
  });
}

function findLikeCount(queryClient: ReturnType<typeof useQueryClient>, postId: number): number {
  const data = queryClient.getQueryData<{ pages: CursorPage<Post>[] }>(FEED_KEY);
  for (const page of data?.pages ?? []) {
    const post = page.items.find((p) => p.id === postId);
    if (post) return post.likeCount;
  }
  return 0;
}
