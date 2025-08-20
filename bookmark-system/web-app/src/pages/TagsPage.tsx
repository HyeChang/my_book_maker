import React, { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Chip,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Grid,
  IconButton,
} from '@mui/material';
import {
  Add as AddIcon,
  Delete as DeleteIcon,
  Label as LabelIcon,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import bookmarkService, { Tag } from '../services/bookmarkService';

const TagsPage: React.FC = () => {
  const queryClient = useQueryClient();
  const [dialogOpen, setDialogOpen] = useState(false);
  const [tagName, setTagName] = useState('');
  const [tagColor, setTagColor] = useState('#4285F4');

  // 태그 목록 조회
  const { data: tags, isLoading } = useQuery({
    queryKey: ['tags'],
    queryFn: bookmarkService.getAllTags,
  });

  // 모든 북마크 조회 (태그 사용 횟수 계산용)
  const { data: bookmarks } = useQuery({
    queryKey: ['bookmarks'],
    queryFn: bookmarkService.getAllBookmarks,
  });

  // 태그 생성 mutation
  const createMutation = useMutation({
    mutationFn: bookmarkService.createTag,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tags'] });
      handleCloseDialog();
    },
  });

  // 태그 삭제 mutation
  const deleteMutation = useMutation({
    mutationFn: bookmarkService.deleteTag,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tags'] });
      queryClient.invalidateQueries({ queryKey: ['bookmarks'] });
    },
  });

  const handleOpenDialog = () => {
    setTagName('');
    setTagColor('#4285F4');
    setDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setDialogOpen(false);
    setTagName('');
    setTagColor('#4285F4');
  };

  const handleSubmit = () => {
    if (tagName.trim()) {
      createMutation.mutate({
        name: tagName.trim(),
        color: tagColor,
      });
    }
  };

  const handleDelete = (id: string) => {
    if (window.confirm('이 태그를 삭제하시겠습니까? 모든 북마크에서 이 태그가 제거됩니다.')) {
      deleteMutation.mutate(id);
    }
  };

  // 태그 사용 횟수 계산
  const getTagUsageCount = (tagName: string) => {
    if (!bookmarks) return 0;
    return bookmarks.filter(b => b.tags?.includes(tagName)).length;
  };

  const colors = [
    '#4285F4', // Blue
    '#EA4335', // Red
    '#FBBC04', // Yellow
    '#34A853', // Green
    '#9C27B0', // Purple
    '#FF6D00', // Orange
    '#00BCD4', // Cyan
    '#795548', // Brown
    '#607D8B', // Blue Gray
    '#E91E63', // Pink
  ];

  return (
    <Box>
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h5">태그 관리</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={handleOpenDialog}
        >
          새 태그
        </Button>
      </Box>

      <Grid container spacing={3}>
        {/* 태그 통계 카드 */}
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                태그 통계
              </Typography>
              <Box sx={{ mt: 2 }}>
                <Typography variant="body2" color="text.secondary">
                  전체 태그 수
                </Typography>
                <Typography variant="h4">
                  {tags?.length || 0}
                </Typography>
              </Box>
              <Box sx={{ mt: 2 }}>
                <Typography variant="body2" color="text.secondary">
                  태그가 있는 북마크
                </Typography>
                <Typography variant="h4">
                  {bookmarks?.filter(b => b.tags && b.tags.length > 0).length || 0}
                </Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* 태그 목록 카드 */}
        <Grid item xs={12} md={8}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                태그 목록
              </Typography>
              
              {isLoading ? (
                <Typography>로딩 중...</Typography>
              ) : tags && tags.length > 0 ? (
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, mt: 2 }}>
                  {tags.map((tag) => (
                    <Chip
                      key={tag.id}
                      label={
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                          <span>{tag.name}</span>
                          <Typography variant="caption" sx={{ opacity: 0.7 }}>
                            ({getTagUsageCount(tag.name)})
                          </Typography>
                        </Box>
                      }
                      icon={<LabelIcon />}
                      onDelete={() => handleDelete(tag.id!)}
                      sx={{
                        backgroundColor: tag.color ? `${tag.color}20` : undefined,
                        borderColor: tag.color,
                        '& .MuiChip-icon': {
                          color: tag.color,
                        },
                      }}
                      variant="outlined"
                    />
                  ))}
                </Box>
              ) : (
                <Typography color="text.secondary" sx={{ mt: 2 }}>
                  태그가 없습니다. 새 태그를 만들어보세요.
                </Typography>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* 인기 태그 카드 */}
        {tags && tags.length > 0 && (
          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  자주 사용되는 태그
                </Typography>
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, mt: 2 }}>
                  {tags
                    .map(tag => ({
                      ...tag,
                      count: getTagUsageCount(tag.name),
                    }))
                    .sort((a, b) => b.count - a.count)
                    .slice(0, 10)
                    .map((tag) => (
                      <Chip
                        key={tag.id}
                        label={`${tag.name} (${tag.count})`}
                        size="medium"
                        sx={{
                          backgroundColor: tag.color ? `${tag.color}20` : undefined,
                          borderColor: tag.color,
                        }}
                        variant="outlined"
                      />
                    ))}
                </Box>
              </CardContent>
            </Card>
          </Grid>
        )}
      </Grid>

      {/* 태그 생성 다이얼로그 */}
      <Dialog open={dialogOpen} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>새 태그 만들기</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="태그 이름"
            fullWidth
            value={tagName}
            onChange={(e) => setTagName(e.target.value)}
            sx={{ mb: 3 }}
          />
          
          <Typography variant="subtitle2" sx={{ mb: 1 }}>
            태그 색상
          </Typography>
          <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
            {colors.map((color) => (
              <Box
                key={color}
                onClick={() => setTagColor(color)}
                sx={{
                  width: 40,
                  height: 40,
                  backgroundColor: color,
                  borderRadius: 1,
                  cursor: 'pointer',
                  border: tagColor === color ? '3px solid #000' : '1px solid #ccc',
                  '&:hover': {
                    opacity: 0.8,
                  },
                }}
              />
            ))}
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>취소</Button>
          <Button onClick={handleSubmit} variant="contained">
            만들기
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default TagsPage;