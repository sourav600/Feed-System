export type Visibility = "PUBLIC" | "PRIVATE";

export interface UserSummary {
  id: number;
  firstName: string;
  lastName: string;
  avatarUrl: string | null;
}

export interface CurrentUser {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  avatarUrl: string | null;
}

export interface Post {
  id: number;
  author: UserSummary;
  content: string;
  imageUrl: string | null;
  visibility: Visibility;
  likeCount: number;
  commentCount: number;
  likedByCurrentUser: boolean;
  createdAt: string;
}

export interface Comment {
  id: number;
  postId: number;
  parentCommentId: number | null;
  author: UserSummary;
  content: string;
  likeCount: number;
  replyCount: number;
  likedByCurrentUser: boolean;
  createdAt: string;
}

export interface LikerSummary {
  userId: number;
  firstName: string;
  lastName: string;
  avatarUrl: string | null;
  likedAt: string;
}

export interface LikeAction {
  liked: boolean;
  likeCount: number;
}

export interface CursorPage<T> {
  items: T[];
  nextCursor: string | null;
  hasMore: boolean;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  fieldErrors?: { field: string; message: string }[] | null;
}
