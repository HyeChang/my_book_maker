import React, { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  IconButton,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  ListItemSecondaryAction,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Fab,
  Chip,
  Menu,
  MenuItem,
} from '@mui/material';
import {
  Folder as FolderIcon,
  FolderOpen as FolderOpenIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Lock as LockIcon,
  LockOpen as LockOpenIcon,
  Add as AddIcon,
  MoreVert as MoreIcon,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import bookmarkService, { Folder } from '../services/bookmarkService';

const FoldersPage: React.FC = () => {
  const queryClient = useQueryClient();
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingFolder, setEditingFolder] = useState<Folder | null>(null);
  const [formData, setFormData] = useState<Folder>({
    name: '',
    color: '#4285F4',
    icon: 'folder',
  });
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [selectedFolder, setSelectedFolder] = useState<string | null>(null);

  // 폴더 목록 조회
  const { data: folders, isLoading } = useQuery({
    queryKey: ['folders'],
    queryFn: bookmarkService.getAllFolders,
  });

  // 각 폴더의 북마크 수 조회
  const { data: allBookmarks } = useQuery({
    queryKey: ['bookmarks'],
    queryFn: bookmarkService.getAllBookmarks,
  });

  // 폴더 생성/수정 mutation
  const saveMutation = useMutation({
    mutationFn: async (data: Folder) => {
      if (editingFolder) {
        return await bookmarkService.updateFolder(editingFolder.id!, data);
      }
      return await bookmarkService.createFolder(data);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['folders'] });
      handleCloseDialog();
    },
  });

  // 폴더 삭제 mutation
  const deleteMutation = useMutation({
    mutationFn: bookmarkService.deleteFolder,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['folders'] });
      queryClient.invalidateQueries({ queryKey: ['bookmarks'] });
    },
  });

  const handleOpenDialog = (folder?: Folder) => {
    if (folder) {
      setEditingFolder(folder);
      setFormData(folder);
    } else {
      setEditingFolder(null);
      setFormData({
        name: '',
        color: '#4285F4',
        icon: 'folder',
      });
    }
    setDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setDialogOpen(false);
    setEditingFolder(null);
    setFormData({
      name: '',
      color: '#4285F4',
      icon: 'folder',
    });
  };

  const handleSubmit = () => {
    if (formData.name.trim()) {
      saveMutation.mutate(formData);
    }
  };

  const handleDelete = (id: string) => {
    if (window.confirm('이 폴더를 삭제하시겠습니까? 폴더 내의 북마크는 기본 폴더로 이동됩니다.')) {
      deleteMutation.mutate(id);
    }
    handleMenuClose();
  };

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>, folderId: string) => {
    setAnchorEl(event.currentTarget);
    setSelectedFolder(folderId);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
    setSelectedFolder(null);
  };

  const getBookmarkCount = (folderId: string) => {
    return allBookmarks?.filter(b => b.folderId === folderId).length || 0;
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
  ];

  return (
    <Box>
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h5">폴더 관리</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => handleOpenDialog()}
        >
          새 폴더
        </Button>
      </Box>

      <Card>
        <CardContent>
          {isLoading ? (
            <Typography>로딩 중...</Typography>
          ) : folders && folders.length > 0 ? (
            <List>
              {folders.map((folder) => (
                <ListItem
                  key={folder.id}
                  sx={{
                    '&:hover': {
                      backgroundColor: 'action.hover',
                    },
                  }}
                >
                  <ListItemIcon>
                    <Box sx={{ color: folder.color }}>
                      {folder.isLocked ? <LockIcon /> : <FolderIcon />}
                    </Box>
                  </ListItemIcon>
                  <ListItemText
                    primary={folder.name}
                    secondary={`${getBookmarkCount(folder.id!)}개의 북마크`}
                  />
                  <ListItemSecondaryAction>
                    <Chip
                      label={folder.isLocked ? '잠김' : '공개'}
                      size="small"
                      icon={folder.isLocked ? <LockIcon /> : <LockOpenIcon />}
                      sx={{ mr: 1 }}
                    />
                    <IconButton
                      edge="end"
                      onClick={(e) => handleMenuOpen(e, folder.id!)}
                    >
                      <MoreIcon />
                    </IconButton>
                  </ListItemSecondaryAction>
                </ListItem>
              ))}
            </List>
          ) : (
            <Typography color="text.secondary">
              폴더가 없습니다. 새 폴더를 만들어보세요.
            </Typography>
          )}
        </CardContent>
      </Card>

      {/* 폴더 메뉴 */}
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleMenuClose}
      >
        <MenuItem
          onClick={() => {
            const folder = folders?.find(f => f.id === selectedFolder);
            if (folder) {
              handleOpenDialog(folder);
            }
            handleMenuClose();
          }}
        >
          <EditIcon sx={{ mr: 1 }} /> 수정
        </MenuItem>
        <MenuItem
          onClick={() => {
            if (selectedFolder) {
              handleDelete(selectedFolder);
            }
          }}
        >
          <DeleteIcon sx={{ mr: 1 }} /> 삭제
        </MenuItem>
      </Menu>

      {/* 폴더 생성/수정 다이얼로그 */}
      <Dialog open={dialogOpen} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>
          {editingFolder ? '폴더 수정' : '새 폴더 만들기'}
        </DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="폴더 이름"
            fullWidth
            value={formData.name}
            onChange={(e) => setFormData(prev => ({ ...prev, name: e.target.value }))}
            sx={{ mb: 3 }}
          />
          
          <Typography variant="subtitle2" sx={{ mb: 1 }}>
            폴더 색상
          </Typography>
          <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
            {colors.map((color) => (
              <Box
                key={color}
                onClick={() => setFormData(prev => ({ ...prev, color }))}
                sx={{
                  width: 40,
                  height: 40,
                  backgroundColor: color,
                  borderRadius: 1,
                  cursor: 'pointer',
                  border: formData.color === color ? '3px solid #000' : '1px solid #ccc',
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
            {editingFolder ? '수정' : '만들기'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default FoldersPage;