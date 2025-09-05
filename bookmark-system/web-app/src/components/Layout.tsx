import React, { useState } from 'react';
import { Outlet, useNavigate, Link } from 'react-router-dom';
import {
  AppBar,
  Box,
  Drawer,
  IconButton,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Typography,
  InputBase,
  Avatar,
  Menu,
  MenuItem,
  Divider,
  alpha,
  styled,
} from '@mui/material';
import {
  Menu as MenuIcon,
  Bookmark as BookmarkIcon,
  Folder as FolderIcon,
  Label as TagIcon,
  Settings as SettingsIcon,
  Search as SearchIcon,
  Add as AddIcon,
  Sync as SyncIcon,
} from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';

/**
 * 사이드바(Drawer) 너비 상수
 * 데스크톱 화면에서 왼쪽 네비게이션 메뉴의 고정 너비
 */
const drawerWidth = 240;

/**
 * 검색 바 스타일 컴포넌트
 * 
 * Material-UI의 styled API를 사용하여 커스텀 스타일링 적용
 * - 반투명 배경색 (15% 투명도의 흰색)
 * - 호버 시 배경색 변화 (25% 투명도)
 * - 반응형 디자인: 모바일에서는 전체 너비, 태블릿 이상에서는 자동 너비
 */
const Search = styled('div')(({ theme }) => ({
  position: 'relative',
  borderRadius: theme.shape.borderRadius,
  backgroundColor: alpha(theme.palette.common.white, 0.15),
  '&:hover': {
    backgroundColor: alpha(theme.palette.common.white, 0.25),
  },
  marginLeft: 0,
  width: '100%',
  [theme.breakpoints.up('sm')]: {  // 600px 이상
    marginLeft: theme.spacing(1),
    width: 'auto',
  },
}));

/**
 * 검색 아이콘 래퍼 스타일 컴포넌트
 * 
 * 검색 입력 필드 왼쪽에 위치하는 돋보기 아이콘 컨테이너
 * - 절대 위치로 입력 필드 위에 오버레이
 * - pointerEvents: 'none'으로 클릭 이벤트가 입력 필드로 전달되도록 설정
 */
const SearchIconWrapper = styled('div')(({ theme }) => ({
  padding: theme.spacing(0, 2),
  height: '100%',
  position: 'absolute',
  pointerEvents: 'none',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
}));

/**
 * 검색 입력 필드 스타일 컴포넌트
 * 
 * Material-UI InputBase 컴포넌트를 커스터마이징
 * - 왼쪽 패딩으로 아이콘 공간 확보
 * - 포커스 시 너비 증가 애니메이션 (20ch → 30ch)
 * - ch 단위: 문자 너비 기준 (character unit)
 */
const StyledInputBase = styled(InputBase)(({ theme }) => ({
  color: 'inherit',
  '& .MuiInputBase-input': {
    padding: theme.spacing(1, 1, 1, 0),
    paddingLeft: `calc(1em + ${theme.spacing(4)})`,  // 아이콘 공간 + 여백
    transition: theme.transitions.create('width'),
    width: '100%',
    [theme.breakpoints.up('sm')]: {  // 태블릿 이상
      width: '20ch',  // 기본 20문자 너비
      '&:focus': {
        width: '30ch',  // 포커스 시 30문자 너비로 확장
      },
    },
  },
}));

/**
 * 메인 레이아웃 컴포넌트
 * 
 * 애플리케이션의 전체 레이아웃 구조를 정의
 * - 상단 AppBar (헤더)
 * - 왼쪽 Drawer (사이드바 네비게이션)
 * - 메인 콘텐츠 영역 (Outlet)
 * 
 * 반응형 디자인:
 * - 모바일: 햄버거 메뉴로 Drawer 토글
 * - 데스크톱: 고정된 Drawer
 */
const Layout: React.FC = () => {
  // 모바일 Drawer 열림/닫힘 상태 관리
  const [mobileOpen, setMobileOpen] = useState(false);
  
  // 사용자 메뉴 앵커 엘리먼트 (프로필 클릭 시 메뉴 위치 결정)
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  
  // 검색어 입력 상태
  const [searchQuery, setSearchQuery] = useState('');
  
  // React Router 네비게이션 훅
  const navigate = useNavigate();
  
  // 인증 컨텍스트에서 사용자 정보와 로그아웃 함수 가져오기
  const { user, logout } = useAuth();

  /**
   * 모바일 Drawer 토글 핸들러
   * 햄버거 메뉴 클릭 시 사이드바 열기/닫기
   */
  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  /**
   * 사용자 프로필 메뉴 열기 핸들러
   * 프로필 아바타 클릭 시 드롭다운 메뉴 표시
   */
  const handleUserMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  /**
   * 사용자 프로필 메뉴 닫기 핸들러
   */
  const handleUserMenuClose = () => {
    setAnchorEl(null);
  };

  /**
   * 로그아웃 핸들러
   * 메뉴를 닫고 로그아웃 처리
   */
  const handleLogout = async () => {
    handleUserMenuClose();
    await logout();
  };

  /**
   * 검색 실행 핸들러
   * Enter 키 입력 시 검색 페이지로 이동
   * 
   * @param event - 키보드 이벤트
   */
  const handleSearch = (event: React.KeyboardEvent) => {
    if (event.key === 'Enter' && searchQuery.trim()) {
      // URL 인코딩하여 검색 쿼리 파라미터 전달
      navigate(`/search?q=${encodeURIComponent(searchQuery)}`);
    }
  };

  /**
   * Drawer 내용 컴포넌트
   * 사이드바에 표시되는 네비게이션 메뉴 구조
   * 
   * 구성:
   * - 헤더: 앱 타이틀
   * - 메인 메뉴: 북마크, 폴더, 태그
   * - 하단 메뉴: 설정
   */
  const drawer = (
    <div>
      {/* Drawer 헤더 영역 */}
      <Toolbar>
        <Typography variant="h6" noWrap component="div">
          북마크 관리
        </Typography>
      </Toolbar>
      <Divider />
      
      {/* 메인 네비게이션 메뉴 */}
      <List>
        {/* 모든 북마크 메뉴 */}
        <ListItem disablePadding>
          <ListItemButton component={Link} to="/">
            <ListItemIcon>
              <BookmarkIcon />
            </ListItemIcon>
            <ListItemText primary="모든 북마크" />
          </ListItemButton>
        </ListItem>
        
        {/* 폴더 관리 메뉴 */}
        <ListItem disablePadding>
          <ListItemButton component={Link} to="/folders">
            <ListItemIcon>
              <FolderIcon />
            </ListItemIcon>
            <ListItemText primary="폴더" />
          </ListItemButton>
        </ListItem>
        
        {/* 태그 관리 메뉴 */}
        <ListItem disablePadding>
          <ListItemButton component={Link} to="/tags">
            <ListItemIcon>
              <TagIcon />
            </ListItemIcon>
            <ListItemText primary="태그" />
          </ListItemButton>
        </ListItem>
      </List>
      
      <Divider />
      
      {/* 하단 메뉴 */}
      <List>
        <ListItem disablePadding>
          <ListItemButton component={Link} to="/settings">
            <ListItemIcon>
              <SettingsIcon />
            </ListItemIcon>
            <ListItemText primary="설정" />
          </ListItemButton>
        </ListItem>
      </List>
    </div>
  );

  return (
    // 최상위 컨테이너 - Flexbox 레이아웃
    <Box sx={{ display: 'flex' }}>
      
      {/* 상단 고정 AppBar (헤더) */}
      <AppBar
        position="fixed"
        sx={{
          // 데스크톱: Drawer 너비만큼 AppBar 너비 감소
          width: { sm: `calc(100% - ${drawerWidth}px)` },
          // 데스크톱: Drawer 너비만큼 왼쪽 마진
          ml: { sm: `${drawerWidth}px` },
        }}
      >
        <Toolbar>
          {/* 모바일 전용 햄버거 메뉴 버튼 */}
          <IconButton
            color="inherit"
            aria-label="open drawer"
            edge="start"
            onClick={handleDrawerToggle}
            sx={{ mr: 2, display: { sm: 'none' } }}  // 태블릿 이상에서 숨김
          >
            <MenuIcon />
          </IconButton>
          
          {/* 검색 바 */}
          <Search>
            <SearchIconWrapper>
              <SearchIcon />
            </SearchIconWrapper>
            <StyledInputBase
              placeholder="북마크 검색…"
              inputProps={{ 'aria-label': 'search' }}
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              onKeyPress={handleSearch}
            />
          </Search>

          {/* 중앙 공간 채우기 (오른쪽 정렬을 위함) */}
          <Box sx={{ flexGrow: 1 }} />

          {/* 새 북마크 추가 버튼 */}
          <IconButton
            color="inherit"
            onClick={() => navigate('/bookmarks/new')}
            sx={{ mr: 1 }}
          >
            <AddIcon />
          </IconButton>

          {/* 동기화 버튼 (기능 미구현) */}
          <IconButton
            color="inherit"
            onClick={() => {/* TODO: 동기화 기능 */}}
            sx={{ mr: 1 }}
          >
            <SyncIcon />
          </IconButton>

          {/* 사용자 프로필 영역 - 로그인 상태일 때만 표시 */}
          {user && (
            <>
              {/* 프로필 아바타 버튼 */}
              <IconButton onClick={handleUserMenuOpen} sx={{ p: 0 }}>
                <Avatar alt={user.name} src={user.picture} />
              </IconButton>
              
              {/* 프로필 드롭다운 메뉴 */}
              <Menu
                anchorEl={anchorEl}
                open={Boolean(anchorEl)}
                onClose={handleUserMenuClose}
              >
                {/* 사용자 이메일 표시 (비활성화) */}
                <MenuItem disabled>
                  <Typography variant="body2">{user.email}</Typography>
                </MenuItem>
                <Divider />
                {/* 설정 메뉴 */}
                <MenuItem onClick={() => { handleUserMenuClose(); navigate('/settings'); }}>
                  설정
                </MenuItem>
                {/* 로그아웃 메뉴 */}
                <MenuItem onClick={handleLogout}>로그아웃</MenuItem>
              </Menu>
            </>
          )}
        </Toolbar>
      </AppBar>

      {/* 사이드바 네비게이션 컨테이너 */}
      <Box
        component="nav"
        sx={{ width: { sm: drawerWidth }, flexShrink: { sm: 0 } }}
      >
        {/* 모바일용 임시 Drawer - 오버레이 형태 */}
        <Drawer
          variant="temporary"  // 임시 Drawer (모달 형태)
          open={mobileOpen}
          onClose={handleDrawerToggle}
          ModalProps={{
            keepMounted: true,  // DOM에 유지하여 성능 개선
          }}
          sx={{
            display: { xs: 'block', sm: 'none' },  // 모바일에서만 표시
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
          }}
        >
          {drawer}
        </Drawer>
        
        {/* 데스크톱용 고정 Drawer */}
        <Drawer
          variant="permanent"  // 항상 표시되는 고정 Drawer
          sx={{
            display: { xs: 'none', sm: 'block' },  // 데스크톱에서만 표시
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
          }}
          open
        >
          {drawer}
        </Drawer>
      </Box>

      {/* 메인 콘텐츠 영역 */}
      <Box
        component="main"
        sx={{
          flex: '1 1 auto',  // flex-grow, flex-shrink, flex-basis
          p: 3,  // 패딩 24px (theme.spacing(3))
          mt: 8,  // AppBar 높이만큼 상단 마진 (64px)
          minWidth: 0,  // flexbox overflow 방지
          overflow: 'hidden',  // 오버플로우 처리
        }}
      >
        {/* 
          React Router Outlet
          하위 라우트 컴포넌트가 이 위치에 렌더링됨
          예: BookmarksPage, FoldersPage, TagsPage 등
        */}
        <Outlet />
      </Box>
    </Box>
  );
};

export default Layout;