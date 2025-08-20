import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
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
  InputAdornment,
  Link,
  Paper,
  Divider,
} from '@mui/material';
import {
  Search as SearchIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  OpenInNew as OpenIcon,
  Clear as ClearIcon,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import bookmarkService, { Bookmark } from '../services/bookmarkService';

const SearchPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [searchQuery, setSearchQuery] = useState(searchParams.get('q') || '');
  const [localQuery, setLocalQuery] = useState(searchParams.get('q') || '');

  // 북마크 검색
  const { data: searchResults, isLoading } = useQuery({
    queryKey: ['bookmarks', 'search', searchQuery],
    queryFn: async () => {
      if (!searchQuery) {
        return await bookmarkService.getAllBookmarks();
      }
      return await bookmarkService.searchBookmarks(searchQuery);
    },
    enabled: true,
  });

  // 북마크 삭제
  const deleteMutation = useMutation({
    mutationFn: bookmarkService.deleteBookmark,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['bookmarks'] });
    },
  });

  useEffect(() => {
    const query = searchParams.get('q');
    if (query) {
      setSearchQuery(query);
      setLocalQuery(query);
    }
  }, [searchParams]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (localQuery.trim()) {
      setSearchParams({ q: localQuery.trim() });
      setSearchQuery(localQuery.trim());
    } else {
      setSearchParams({});
      setSearchQuery('');
    }
  };

  const handleClear = () => {
    setLocalQuery('');
    setSearchQuery('');
    setSearchParams({});
  };

  const handleDelete = async (id: string) => {
    if (window.confirm('이 북마크를 삭제하시겠습니까?')) {
      deleteMutation.mutate(id);
    }
  };

  const handleEdit = (id: string) => {
    navigate(`/bookmarks/edit/${id}`);
  };

  // 검색어 하이라이팅
  const highlightText = (text: string, query: string) => {
    if (!query) return text;
    
    const parts = text.split(new RegExp(`(${query})`, 'gi'));
    return (
      <>
        {parts.map((part, index) =>
          part.toLowerCase() === query.toLowerCase() ? (
            <mark key={index} style={{ backgroundColor: '#FBBC04', padding: '0 2px' }}>
              {part}
            </mark>
          ) : (
            part
          )
        )}
      </>
    );
  };

  return (
    <Box>
      <Paper elevation={0} sx={{ p: 3, mb: 3, backgroundColor: 'background.default' }}>
        <form onSubmit={handleSearch}>
          <TextField
            fullWidth
            variant="outlined"
            placeholder="북마크 검색... (제목, URL, 설명, 태그)"
            value={localQuery}
            onChange={(e) => setLocalQuery(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              ),
              endAdornment: localQuery && (
                <InputAdornment position="end">
                  <IconButton size="small" onClick={handleClear}>
                    <ClearIcon />
                  </IconButton>
                </InputAdornment>
              ),
            }}
            sx={{
              '& .MuiOutlinedInput-root': {
                backgroundColor: 'background.paper',
              },
            }}
          />
        </form>
        
        {searchQuery && (
          <Box sx={{ mt: 2 }}>
            <Typography variant="body2" color="text.secondary">
              "{searchQuery}" 검색 결과: {searchResults?.length || 0}개
            </Typography>
          </Box>
        )}
      </Paper>

      {isLoading ? (
        <Typography>검색 중...</Typography>
      ) : searchResults && searchResults.length > 0 ? (
        <Grid container spacing={3}>
          {searchResults.map((bookmark) => (
            <Grid item xs={12} sm={6} md={4} key={bookmark.id}>
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
                      {searchQuery ? highlightText(bookmark.title, searchQuery) : bookmark.title}
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
                    {searchQuery && bookmark.description
                      ? highlightText(bookmark.description, searchQuery)
                      : bookmark.description || bookmark.url}
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
                    {searchQuery ? highlightText(bookmark.url, searchQuery) : bookmark.url}
                  </Link>

                  {bookmark.tags && bookmark.tags.length > 0 && (
                    <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                      {bookmark.tags.map((tag, index) => (
                        <Chip
                          key={index}
                          label={searchQuery ? highlightText(tag, searchQuery) : tag}
                          size="small"
                          variant="outlined"
                          onClick={() => {
                            setLocalQuery(tag);
                            setSearchQuery(tag);
                            setSearchParams({ q: tag });
                          }}
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
      ) : (
        <Paper sx={{ p: 4, textAlign: 'center' }}>
          <Typography variant="h6" color="text.secondary" gutterBottom>
            {searchQuery ? '검색 결과가 없습니다' : '검색어를 입력해주세요'}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {searchQuery
              ? '다른 검색어를 시도해보세요'
              : '제목, URL, 설명, 태그로 북마크를 검색할 수 있습니다'}
          </Typography>
        </Paper>
      )}
    </Box>
  );
};

export default SearchPage;