import api from './api';

export interface Bookmark {
  id?: string;
  url: string;
  title: string;
  description?: string;
  favicon?: string;
  folderId?: string;
  tags?: string[];
  createdAt?: string;
  updatedAt?: string;
  metadata?: {
    visitCount?: number;
    lastVisited?: string;
  };
}

export interface Folder {
  id?: string;
  name: string;
  parentId?: string | null;
  isLocked?: boolean;
  passwordHash?: string;
  color?: string;
  icon?: string;
  order?: number;
}

export interface Tag {
  id?: string;
  name: string;
  color?: string;
  usageCount?: number;
}

export interface BookmarkData {
  version?: string;
  lastModified?: string;
  bookmarks: Bookmark[];
  folders: Folder[];
  tags: Tag[];
}

const bookmarkService = {
  // Bookmark operations
  getAllBookmarks: async (): Promise<Bookmark[]> => {
    const response = await api.get('/bookmarks');
    return response.data;
  },

  getBookmarkById: async (id: string): Promise<Bookmark> => {
    const response = await api.get(`/bookmarks/${id}`);
    return response.data;
  },

  createBookmark: async (bookmark: Bookmark): Promise<Bookmark> => {
    const response = await api.post('/bookmarks', bookmark);
    return response.data;
  },

  updateBookmark: async (id: string, bookmark: Bookmark): Promise<Bookmark> => {
    const response = await api.put(`/bookmarks/${id}`, bookmark);
    return response.data;
  },

  deleteBookmark: async (id: string): Promise<void> => {
    await api.delete(`/bookmarks/${id}`);
  },

  searchBookmarks: async (query: string): Promise<Bookmark[]> => {
    const response = await api.get('/bookmarks/search', { params: { q: query } });
    return response.data;
  },

  getBookmarksByFolder: async (folderId: string): Promise<Bookmark[]> => {
    const response = await api.get(`/bookmarks/folder/${folderId}`);
    return response.data;
  },

  getBookmarksByTag: async (tag: string): Promise<Bookmark[]> => {
    const response = await api.get(`/bookmarks/tag/${tag}`);
    return response.data;
  },

  // Folder operations
  getAllFolders: async (): Promise<Folder[]> => {
    const response = await api.get('/folders');
    return response.data;
  },

  createFolder: async (folder: Folder): Promise<Folder> => {
    const response = await api.post('/folders', folder);
    return response.data;
  },

  updateFolder: async (id: string, folder: Folder): Promise<Folder> => {
    const response = await api.put(`/folders/${id}`, folder);
    return response.data;
  },

  deleteFolder: async (id: string): Promise<void> => {
    await api.delete(`/folders/${id}`);
  },

  lockFolder: async (id: string, password: string): Promise<void> => {
    await api.put(`/folders/${id}/lock`, { password });
  },

  // Tag operations
  getAllTags: async (): Promise<Tag[]> => {
    const response = await api.get('/tags');
    return response.data;
  },

  createTag: async (tag: Tag): Promise<Tag> => {
    const response = await api.post('/tags', tag);
    return response.data;
  },

  deleteTag: async (id: string): Promise<void> => {
    await api.delete(`/tags/${id}`);
  },

  // Sync operations
  syncWithDrive: async (): Promise<void> => {
    await api.post('/sync');
  },

  createBackup: async (): Promise<void> => {
    await api.post('/backup');
  },

  getBackups: async (): Promise<any[]> => {
    const response = await api.get('/backup');
    return response.data;
  },
};

export default bookmarkService;