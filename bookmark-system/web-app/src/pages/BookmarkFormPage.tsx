import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Box,
  TextField,
  Button,
  Card,
  CardContent,
  Typography,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  Stack,
  Alert,
  CircularProgress,
} from '@mui/material';
import { Save as SaveIcon, Cancel as CancelIcon } from '@mui/icons-material';
import { useQuery, useMutation } from '@tanstack/react-query';
import bookmarkService, { Bookmark, Folder, Tag } from '../services/bookmarkService';

const BookmarkFormPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEdit = !!id;

  const [formData, setFormData] = useState<Bookmark>({
    url: '',
    title: '',
    description: '',
    folderId: '',
    tags: [],
  });
  const [tagInput, setTagInput] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // 기존 북마크 조회 (수정 모드)
  const { data: existingBookmark } = useQuery({
    queryKey: ['bookmark', id],
    queryFn: () => bookmarkService.getBookmarkById(id!),
    enabled: isEdit,
  });

  // 폴더 목록 조회
  const { data: folders } = useQuery({
    queryKey: ['folders'],
    queryFn: bookmarkService.getAllFolders,
  });

  // 태그 목록 조회
  const { data: tags } = useQuery({
    queryKey: ['tags'],
    queryFn: bookmarkService.getAllTags,
  });

  // 북마크 생성/수정 mutation
  const saveMutation = useMutation({
    mutationFn: async (data: Bookmark) => {
      if (isEdit) {
        return await bookmarkService.updateBookmark(id!, data);
      }
      return await bookmarkService.createBookmark(data);
    },
    onSuccess: () => {
      navigate('/');
    },
    onError: (error: any) => {
      setError(error.response?.data?.message || '저장 중 오류가 발생했습니다.');
    },
  });

  useEffect(() => {
    if (existingBookmark) {
      setFormData(existingBookmark);
    }
  }, [existingBookmark]);

  // URL에서 타이틀과 파비콘 자동 추출
  const fetchUrlMetadata = async () => {
    if (!formData.url) {
      setError('URL을 입력해주세요.');
      return;
    }

    setLoading(true);
    try {
      // TODO: 백엔드에 URL 메타데이터 추출 API 추가 필요
      // 임시로 URL을 타이틀로 사용
      if (!formData.title) {
        const url = new URL(formData.url);
        setFormData(prev => ({
          ...prev,
          title: url.hostname,
        }));
      }
    } catch (err) {
      setError('유효한 URL을 입력해주세요.');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!formData.url || !formData.title) {
      setError('URL과 제목은 필수 입력 항목입니다.');
      return;
    }

    saveMutation.mutate(formData);
  };

  const handleAddTag = () => {
    if (tagInput.trim() && !formData.tags?.includes(tagInput.trim())) {
      setFormData(prev => ({
        ...prev,
        tags: [...(prev.tags || []), tagInput.trim()],
      }));
      setTagInput('');
    }
  };

  const handleRemoveTag = (tagToRemove: string) => {
    setFormData(prev => ({
      ...prev,
      tags: prev.tags?.filter(tag => tag !== tagToRemove) || [],
    }));
  };

  return (
    <Box sx={{ maxWidth: 800, mx: 'auto' }}>
      <Card>
        <CardContent>
          <Typography variant="h5" component="h1" gutterBottom>
            {isEdit ? '북마크 수정' : '새 북마크 추가'}
          </Typography>

          {error && (
            <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>
              {error}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit}>
            <Stack spacing={3}>
              <Box sx={{ display: 'flex', gap: 1 }}>
                <TextField
                  fullWidth
                  label="URL"
                  value={formData.url}
                  onChange={(e) => setFormData(prev => ({ ...prev, url: e.target.value }))}
                  required
                  placeholder="https://example.com"
                />
                <Button
                  variant="outlined"
                  onClick={fetchUrlMetadata}
                  disabled={loading}
                  sx={{ minWidth: 120 }}
                >
                  {loading ? <CircularProgress size={24} /> : '정보 가져오기'}
                </Button>
              </Box>

              <TextField
                fullWidth
                label="제목"
                value={formData.title}
                onChange={(e) => setFormData(prev => ({ ...prev, title: e.target.value }))}
                required
              />

              <TextField
                fullWidth
                label="설명"
                value={formData.description}
                onChange={(e) => setFormData(prev => ({ ...prev, description: e.target.value }))}
                multiline
                rows={3}
              />

              <FormControl fullWidth>
                <InputLabel>폴더</InputLabel>
                <Select
                  value={formData.folderId || ''}
                  label="폴더"
                  onChange={(e) => setFormData(prev => ({ ...prev, folderId: e.target.value }))}
                >
                  <MenuItem value="">
                    <em>없음</em>
                  </MenuItem>
                  {folders?.map((folder) => (
                    <MenuItem key={folder.id} value={folder.id}>
                      {folder.name}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>

              <Box>
                <Box sx={{ display: 'flex', gap: 1, mb: 1 }}>
                  <TextField
                    fullWidth
                    label="태그 추가"
                    value={tagInput}
                    onChange={(e) => setTagInput(e.target.value)}
                    onKeyPress={(e) => {
                      if (e.key === 'Enter') {
                        e.preventDefault();
                        handleAddTag();
                      }
                    }}
                    placeholder="태그를 입력하고 Enter 키를 누르세요"
                  />
                  <Button variant="outlined" onClick={handleAddTag}>
                    추가
                  </Button>
                </Box>
                
                {formData.tags && formData.tags.length > 0 && (
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                    {formData.tags.map((tag, index) => (
                      <Chip
                        key={index}
                        label={tag}
                        onDelete={() => handleRemoveTag(tag)}
                      />
                    ))}
                  </Box>
                )}
              </Box>

              <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
                <Button
                  variant="outlined"
                  startIcon={<CancelIcon />}
                  onClick={() => navigate('/')}
                >
                  취소
                </Button>
                <Button
                  type="submit"
                  variant="contained"
                  startIcon={<SaveIcon />}
                  disabled={saveMutation.isPending}
                >
                  {saveMutation.isPending ? '저장 중...' : '저장'}
                </Button>
              </Box>
            </Stack>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
};

export default BookmarkFormPage;