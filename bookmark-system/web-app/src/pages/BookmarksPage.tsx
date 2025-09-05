import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  CardActions,
  Typography,
  IconButton,
  Grid,
  Chip,
  Avatar,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Fab,
  Link,
  Skeleton,
} from '@mui/material';
import type { SelectChangeEvent } from '@mui/material/Select';
import {
  Edit as EditIcon,
  Delete as DeleteIcon,
  OpenInNew as OpenIcon,
  Add as AddIcon,
  FilterList as FilterIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import bookmarkService from '../services/bookmarkService';
import type { Bookmark, Folder } from '../services/bookmarkService';

const BookmarksPage: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [selectedFolder, setSelectedFolder] = useState<string>('all');
  const [searchQuery, setSearchQuery] = useState('');

  // 북마크 조회
  const { data: bookmarks, isLoading: bookmarksLoading } = useQuery({
    queryKey: ['bookmarks', selectedFolder],
    queryFn: async () => {
      if (selectedFolder === 'all') {
        return await bookmarkService.getAllBookmarks();
      }
      return await bookmarkService.getBookmarksByFolder(selectedFolder);
    },
  });

  // 폴더 조회
  const { data: folders } = useQuery({
    queryKey: ['folders'],
    queryFn: bookmarkService.getAllFolders,
  });

  // 북마크 삭제
  const deleteMutation = useMutation({
    mutationFn: bookmarkService.deleteBookmark,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['bookmarks'] });
    },
  });

  const handleFolderChange = (event: SelectChangeEvent) => {
    setSelectedFolder(event.target.value);
  };

  const handleDelete = async (id: string) => {
    if (window.confirm('이 북마크를 삭제하시겠습니까?')) {
      deleteMutation.mutate(id);
    }
  };

  const handleEdit = (id: string) => {
    navigate(`/bookmarks/edit/${id}`);
  };

  const filteredBookmarks = bookmarks?.filter((bookmark) => {
    if (!searchQuery) return true;
    const query = searchQuery.toLowerCase();
    return (
      bookmark.title.toLowerCase().includes(query) ||
      bookmark.description?.toLowerCase().includes(query) ||
      bookmark.url.toLowerCase().includes(query) ||
      bookmark.tags?.some(tag => tag.toLowerCase().includes(query))
    );
  });

  return (
    <Box>
      <Box sx={{ mb: 3, display: 'flex', gap: 2, alignItems: 'center' }}>
        <TextField
          size="small"
          placeholder="북마크 검색..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          sx={{ flexGrow: 1, maxWidth: 400 }}
        />
        
        <FormControl size="small" sx={{ minWidth: 200 }}>
          <InputLabel>폴더</InputLabel>
          <Select
            value={selectedFolder}
            label="폴더"
            onChange={handleFolderChange}
          >
            <MenuItem value="all">모든 폴더</MenuItem>
            {folders?.map((folder) => (
              <MenuItem key={folder.id} value={folder.id!}>
                {folder.name}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      </Box>

      {bookmarksLoading ? (
        <Grid container spacing={3}>
          {[1, 2, 3, 4].map((item) => (
            <Grid item xs={12} sm={6} md={4} lg={3} key={item}>
              <Card>
                <CardContent>
                  <Skeleton variant="text" width="60%" />
                  <Skeleton variant="text" />
                  <Skeleton variant="text" />
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      ) : (
        <Grid container spacing={3}>
          {filteredBookmarks?.map((bookmark) => (
            <Grid item xs={12} sm={6} md={4} lg={3} key={bookmark.id}>
              <Card
                sx={{
                  height: '100%',
                  display: 'flex',
                  flexDirection: 'column',
                  '&:hover': {
                    boxShadow: 4,
                  },
                }}
              >
                <CardContent sx={{ flexGrow: 1 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                    <Avatar
                      src={bookmark.favicon}
                      sx={{ width: 24, height: 24, mr: 1 }}
                    >
                      {bookmark.title[0]}
                    </Avatar>
                    <Typography variant="h6" component="h2" noWrap>
                      {bookmark.title}
                    </Typography>
                  </Box>
                  
                  <Typography
                    variant="body2"
                    color="text.secondary"
                    sx={{
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      display: '-webkit-box',
                      WebkitLineClamp: 2,
                      WebkitBoxOrient: 'vertical',
                      mb: 1,
                    }}
                  >
                    {bookmark.description || bookmark.url}
                  </Typography>

                  <Link
                    href={bookmark.url}
                    target="_blank"
                    rel="noopener noreferrer"
                    variant="caption"
                    sx={{
                      display: 'block',
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      whiteSpace: 'nowrap',
                      mb: 1,
                    }}
                  >
                    {bookmark.url}
                  </Link>

                  {bookmark.tags && bookmark.tags.length > 0 && (
                    <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                      {bookmark.tags.map((tag, index) => (
                        <Chip
                          key={index}
                          label={tag}
                          size="small"
                          variant="outlined"
                        />
                      ))}
                    </Box>
                  )}
                </CardContent>
                
                <CardActions>
                  <IconButton
                    size="small"
                    href={bookmark.url}
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    <OpenIcon />
                  </IconButton>
                  <IconButton
                    size="small"
                    onClick={() => handleEdit(bookmark.id!)}
                  >
                    <EditIcon />
                  </IconButton>
                  <IconButton
                    size="small"
                    onClick={() => handleDelete(bookmark.id!)}
                  >
                    <DeleteIcon />
                  </IconButton>
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}

      <Fab
        color="primary"
        aria-label="add"
        sx={{
          position: 'fixed',
          bottom: 16,
          right: 16,
        }}
        onClick={() => navigate('/bookmarks/new')}
      >
        <AddIcon />
      </Fab>
    </Box>
  );
};

export default BookmarksPage;