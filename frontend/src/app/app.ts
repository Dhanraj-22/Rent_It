import { CommonModule } from '@angular/common';
import { HttpClient, HttpErrorResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Observable } from 'rxjs';

type Tab = 'rooms' | 'properties' | 'roommates';
type AuthMode = 'login' | 'register' | 'verify' | 'forgot' | 'reset';
type Composer = 'room' | 'property' | 'roommate' | null;

interface LoginResponse {
  token: string;
  id: number;
  name: string;
  email: string;
  role: 'USER' | 'OWNER' | 'ADMIN' | string;
}

interface UserDto {
  id?: number;
  name: string;
  email: string;
  password?: string;
  role?: string;
  phone?: string;
  active?: boolean;
  emailVerified?: boolean;
}

interface RoomPost {
  id?: number;
  title: string;
  description: string;
  city: string;
  address: string;
  rent: number;
  capacity: number;
  available?: boolean;
  createdAt?: string;
  ownerId?: number;
  ownerName?: string;
  images?: string[];
}

interface PropertyPost {
  id?: number;
  title: string;
  description: string;
  address?: string;
  rent: number;
  deposit?: number;
  city: string;
  bhkType?: string;
  available?: boolean;
  ownerId?: number;
  ownerName?: string;
  images?: string[];
}

interface RoommatePost {
  id?: number;
  title: string;
  description: string;
  city: string;
  rent: string;
  genderPreference?: string;
  foodPreference?: string;
  smoking?: boolean;
  occupation?: string;
  active?: boolean;
  createdAt?: string;
  userId?: number;
  userName?: string;
}

interface RoomInterestRequest {
  id?: number;
  message: string;
  status?: string;
  createdAt?: string;
  roomId?: number;
  userId?: number;
  userName?: string;
}

interface RoommateRequest {
  id?: number;
  message: string;
  status?: string;
  createdAt?: string;
  postId?: number;
  senderId?: number;
  senderName?: string;
}

interface ContactTarget {
  type: 'room' | 'roommate';
  id: number;
  title: string;
  ownerId?: number;
}

@Component({
  selector: 'app-root',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder);
  private readonly sessionKey = 'rent-it-session';

  readonly apiBase = this.readApiBase();
  readonly fallbackImage = `${this.apiBase}/uploads/bce13c5d-0abe-4c6b-afc6-3b6df1a6386c_flate1.jpg`;
  readonly heroImage = `${this.apiBase}/uploads/ad987781-6a01-4791-8412-fd00941e2d68_Room5433_6RESIZE.jpg`;

  readonly auth = signal<LoginResponse | null>(this.readSession());
  readonly activeTab = signal<Tab>('rooms');
  readonly authOpen = signal(false);
  readonly authMode = signal<AuthMode>('login');
  readonly accountMenuOpen = signal(false);
  readonly composer = signal<Composer>(null);
  readonly contact = signal<ContactTarget | null>(null);
  readonly requestsOpen = signal(false);
  readonly rooms = signal<RoomPost[]>([]);
  readonly properties = signal<PropertyPost[]>([]);
  readonly roommates = signal<RoommatePost[]>([]);
  readonly roomRequests = signal<RoomInterestRequest[]>([]);
  readonly roommateRequests = signal<RoommateRequest[]>([]);
  readonly selectedRequestTitle = signal('Requests');
  readonly loading = signal(false);
  readonly listingsLoading = signal(true);
  readonly error = signal('');
  readonly success = signal('');
  readonly roomImages = signal<File[]>([]);
  readonly propertyImages = signal<File[]>([]);

  readonly isOwner = computed(() => {
    const user = this.auth();
    return user?.role === 'OWNER' || user?.role === 'ADMIN';
  });

  readonly activeCount = computed(() => {
    switch (this.activeTab()) {
      case 'properties':
        return this.properties().length;
      case 'roommates':
        return this.roommates().length;
      default:
        return this.rooms().length;
    }
  });

  readonly activeLabel = computed(() => {
    switch (this.activeTab()) {
      case 'properties':
        return 'properties';
      case 'roommates':
        return 'roommate posts';
      default:
        return 'rooms';
    }
  });

  readonly composerTitle = computed(() => {
    switch (this.composer()) {
      case 'property':
        return 'List a property';
      case 'roommate':
        return 'Find a roommate';
      default:
        return 'List a room';
    }
  });

  readonly loginForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  readonly registerForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(80)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(100)]],
    phone: ['', [Validators.maxLength(20)]],
    role: 'USER'
  });

  readonly verifyForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    otp: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]]
  });

  readonly forgotForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]]
  });

  readonly resetForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    otp: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]],
    newPassword: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(100)]]
  });

  readonly searchForm = this.fb.nonNullable.group({
    city: ['', [Validators.maxLength(80)]]
  });

  readonly roomForm = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(120)]],
    description: ['', [Validators.required, Validators.maxLength(2000)]],
    city: ['', [Validators.required, Validators.maxLength(80)]],
    address: ['', [Validators.required, Validators.maxLength(200)]],
    rent: [0, [Validators.required, Validators.min(1)]],
    capacity: [1, [Validators.required, Validators.min(1), Validators.max(50)]]
  });

  readonly propertyForm = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(120)]],
    description: ['', [Validators.required, Validators.maxLength(2000)]],
    address: ['', [Validators.required, Validators.maxLength(200)]],
    city: ['', [Validators.required, Validators.maxLength(80)]],
    rent: [0, [Validators.required, Validators.min(1)]],
    deposit: [0, [Validators.required, Validators.min(0)]],
    bhkType: '1 BHK'
  });

  readonly roommateForm = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(120)]],
    description: ['', [Validators.required, Validators.maxLength(2000)]],
    city: ['', [Validators.required, Validators.maxLength(80)]],
    rent: ['', [Validators.required, Validators.maxLength(40)]],
    genderPreference: 'Any',
    foodPreference: 'Any',
    smoking: false,
    occupation: ['', [Validators.maxLength(80)]]
  });

  readonly contactForm = this.fb.nonNullable.group({
    message: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(500)]]
  });

  private lastRoomRequestId: number | null = null;
  private lastRoommateRequestId: number | null = null;

  ngOnInit(): void {
    this.loadAll();
  }

  selectTab(tab: Tab): void {
    this.activeTab.set(tab);
    this.composer.set(null);
  }

  openAuth(mode: AuthMode = 'login'): void {
    this.accountMenuOpen.set(false);
    this.authMode.set(mode);
    this.authOpen.set(true);
    this.clearMessages();
  }

  closeAuth(): void {
    this.authOpen.set(false);
  }

  setAuthMode(mode: AuthMode): void {
    this.authMode.set(mode);
    this.clearMessages();
  }

  toggleAccountMenu(): void {
    this.accountMenuOpen.update((open) => !open);
  }

  closeNotice(): void {
    this.clearMessages();
  }

  loadAll(): void {
    this.listingsLoading.set(true);
    this.loadRooms();
    this.loadProperties();
    this.loadRoommates();
  }

  refreshActive(): void {
    this.listingsLoading.set(true);
    switch (this.activeTab()) {
      case 'properties':
        this.loadProperties();
        break;
      case 'roommates':
        this.loadRoommates();
        break;
      default:
        this.loadRooms();
    }
  }

  loadRooms(): void {
    this.http.get<RoomPost[]>(`${this.apiBase}/api/rooms`).subscribe({
      next: (rooms) => {
        this.rooms.set(this.sortNewest(rooms ?? []));
        this.listingsLoading.set(false);
      },
      error: (error) => this.setError('Rooms could not be loaded', error, true)
    });
  }

  loadProperties(): void {
    this.http.get<PropertyPost[]>(`${this.apiBase}/api/properties`).subscribe({
      next: (properties) => {
        this.properties.set(properties ?? []);
        this.listingsLoading.set(false);
      },
      error: (error) => this.setError('Properties could not be loaded', error, true)
    });
  }

  loadRoommates(): void {
    this.http.get<RoommatePost[]>(`${this.apiBase}/roommate/getAllPost`).subscribe({
      next: (roommates) => {
        this.roommates.set(this.sortNewest(roommates ?? []));
        this.listingsLoading.set(false);
      },
      error: (error) => this.setError('Roommate posts could not be loaded', error, true)
    });
  }

  searchByCity(): void {
    const city = this.searchForm.getRawValue().city.trim();
    if (!city) {
      this.loadAll();
      return;
    }

    this.listingsLoading.set(true);
    const encodedCity = encodeURIComponent(city);
    this.http.get<RoomPost[]>(`${this.apiBase}/api/rooms/city/${encodedCity}`).subscribe({
      next: (rooms) => this.rooms.set(this.sortNewest(rooms ?? [])),
      error: (error) => this.setError('Room search failed', error, true)
    });
    this.http.get<PropertyPost[]>(`${this.apiBase}/api/properties/city/${encodedCity}`).subscribe({
      next: (properties) => this.properties.set(properties ?? []),
      error: (error) => this.setError('Property search failed', error, true)
    });
    this.http.get<RoommatePost[]>(`${this.apiBase}/roommate/getPostByCity/${encodedCity}`).subscribe({
      next: (roommates) => {
        this.roommates.set(this.sortNewest(roommates ?? []));
        this.listingsLoading.set(false);
      },
      error: (error) => this.setError('Roommate search failed', error, true)
    });
  }

  clearSearch(): void {
    this.searchForm.reset({ city: '' });
    this.loadAll();
  }

  login(): void {
    if (!this.isFormValid(this.loginForm)) {
      return;
    }

    this.loading.set(true);
    this.clearMessages();
    this.http.post<LoginResponse>(`${this.apiBase}/auth/login`, this.loginForm.getRawValue()).subscribe({
      next: (response) => {
        this.auth.set(response);
        localStorage.setItem(this.sessionKey, JSON.stringify(response));
        this.success.set(`Welcome back, ${response.name}.`);
        this.loading.set(false);
        this.authOpen.set(false);
        this.loginForm.reset({ email: response.email, password: '' });
      },
      error: (error) => {
        this.loading.set(false);
        this.setError('Login failed', error);
      }
    });
  }

  register(): void {
    if (!this.isFormValid(this.registerForm)) {
      return;
    }

    this.loading.set(true);
    this.clearMessages();
    const payload: UserDto = this.registerForm.getRawValue();

    this.http.post<UserDto>(`${this.apiBase}/user/register`, payload).subscribe({
      next: (user) => {
        this.verifyForm.patchValue({ email: user.email });
        this.loginForm.patchValue({ email: user.email });
        this.loading.set(false);
        if (user.emailVerified) {
          this.success.set('Account created. Sign in to continue.');
          this.authMode.set('login');
        } else {
          this.success.set('Verification code sent. Enter it to activate your account.');
          this.authMode.set('verify');
        }
      },
      error: (error) => {
        this.loading.set(false);
        this.setError('Registration failed', error);
      }
    });
  }

  verifyOtp(): void {
    if (!this.isFormValid(this.verifyForm)) {
      return;
    }

    this.loading.set(true);
    this.clearMessages();
    const raw = this.verifyForm.getRawValue();
    const params = new HttpParams().set('email', raw.email).set('otp', raw.otp);

    this.http.post<UserDto>(`${this.apiBase}/user/verify-otp`, null, { params }).subscribe({
      next: () => {
        this.success.set('Email verified. Sign in to continue.');
        this.loginForm.patchValue({ email: raw.email });
        this.authMode.set('login');
        this.loading.set(false);
      },
      error: (error) => {
        this.loading.set(false);
        this.setError('Verification failed', error);
      }
    });
  }

  sendPasswordOtp(): void {
    if (!this.isFormValid(this.forgotForm)) {
      return;
    }

    this.loading.set(true);
    this.clearMessages();
    const email = this.forgotForm.getRawValue().email;
    this.http.post(`${this.apiBase}/auth/forget-password`, { email }, { responseType: 'text' }).subscribe({
      next: () => {
        this.resetForm.patchValue({ email });
        this.authMode.set('reset');
        this.success.set('Password reset code sent.');
        this.loading.set(false);
      },
      error: (error) => {
        this.loading.set(false);
        this.setError('Could not send reset code', error);
      }
    });
  }

  resetPassword(): void {
    if (!this.isFormValid(this.resetForm)) {
      return;
    }

    this.loading.set(true);
    this.clearMessages();
    this.http.post(`${this.apiBase}/auth/reset-password`, this.resetForm.getRawValue(), { responseType: 'text' }).subscribe({
      next: () => {
        const email = this.resetForm.getRawValue().email;
        this.loginForm.patchValue({ email, password: '' });
        this.resetForm.reset({ email: '', otp: '', newPassword: '' });
        this.authMode.set('login');
        this.success.set('Password updated. You can now sign in.');
        this.loading.set(false);
      },
      error: (error) => {
        this.loading.set(false);
        this.setError('Password reset failed', error);
      }
    });
  }

  logout(): void {
    this.auth.set(null);
    this.accountMenuOpen.set(false);
    this.composer.set(null);
    this.requestsOpen.set(false);
    localStorage.removeItem(this.sessionKey);
    this.success.set('You have been signed out.');
  }

  openComposer(type: Exclude<Composer, null>): void {
    if (!this.requireUser()) {
      return;
    }
    if ((type === 'room' || type === 'property') && !this.isOwner()) {
      this.error.set('Only OWNER or ADMIN accounts can list rental spaces.');
      return;
    }
    this.composer.set(this.composer() === type ? null : type);
    this.requestsOpen.set(false);
  }

  closeComposer(): void {
    this.composer.set(null);
  }

  onRoomImagesSelected(event: Event): void {
    this.roomImages.set(this.filesFromEvent(event));
  }

  onPropertyImagesSelected(event: Event): void {
    this.propertyImages.set(this.filesFromEvent(event));
  }

  createRoom(): void {
    const user = this.requireUser();
    if (!user || !this.ensureOwnerRole() || !this.isFormValid(this.roomForm)) {
      return;
    }

    const raw = this.roomForm.getRawValue();
    const payload: RoomPost = {
      title: raw.title.trim(),
      description: raw.description.trim(),
      city: raw.city.trim(),
      address: raw.address.trim(),
      rent: Number(raw.rent),
      capacity: Number(raw.capacity),
      available: true
    };

    this.loading.set(true);
    this.clearMessages();
    this.http.post<RoomPost>(`${this.apiBase}/api/rooms/${user.id}`, this.multipart('room', payload, 'images', this.roomImages()), {
      headers: this.authHeaders()
    }).subscribe({
      next: () => {
        this.roomForm.reset({ title: '', description: '', city: '', address: '', rent: 0, capacity: 1 });
        this.roomImages.set([]);
        this.success.set('Room listing published.');
        this.loading.set(false);
        this.composer.set(null);
        this.activeTab.set('rooms');
        this.loadRooms();
      },
      error: (error) => {
        this.loading.set(false);
        this.setError('Room could not be published', error);
      }
    });
  }

  createProperty(): void {
    const user = this.requireUser();
    if (!user || !this.ensureOwnerRole() || !this.isFormValid(this.propertyForm)) {
      return;
    }

    const raw = this.propertyForm.getRawValue();
    const payload: PropertyPost = {
      title: raw.title.trim(),
      description: raw.description.trim(),
      address: raw.address.trim(),
      city: raw.city.trim(),
      rent: Number(raw.rent),
      deposit: Number(raw.deposit),
      bhkType: raw.bhkType,
      available: true,
      ownerId: user.id
    };

    this.loading.set(true);
    this.clearMessages();
    this.http.post<PropertyPost>(`${this.apiBase}/api/properties/add/${user.id}`, this.multipart('property', payload, 'files', this.propertyImages()), {
      headers: this.authHeaders()
    }).subscribe({
      next: () => {
        this.propertyForm.reset({ title: '', description: '', address: '', city: '', rent: 0, deposit: 0, bhkType: '1 BHK' });
        this.propertyImages.set([]);
        this.success.set('Property listing published.');
        this.loading.set(false);
        this.composer.set(null);
        this.activeTab.set('properties');
        this.loadProperties();
      },
      error: (error) => {
        this.loading.set(false);
        this.setError('Property could not be published', error);
      }
    });
  }

  createRoommatePost(): void {
    const user = this.requireUser();
    if (!user || !this.isFormValid(this.roommateForm)) {
      return;
    }

    this.loading.set(true);
    this.clearMessages();
    const raw = this.roommateForm.getRawValue();
    const payload = { ...raw, title: raw.title.trim(), description: raw.description.trim(), city: raw.city.trim(), occupation: raw.occupation.trim() };
    this.http.post<RoommatePost>(`${this.apiBase}/roommate/createPost/${user.id}`, payload, { headers: this.authHeaders() }).subscribe({
      next: () => {
        this.roommateForm.reset({ title: '', description: '', city: '', rent: '', genderPreference: 'Any', foodPreference: 'Any', smoking: false, occupation: '' });
        this.success.set('Roommate post published.');
        this.loading.set(false);
        this.composer.set(null);
        this.activeTab.set('roommates');
        this.loadRoommates();
      },
      error: (error) => {
        this.loading.set(false);
        this.setError('Roommate post could not be published', error);
      }
    });
  }

  openContact(type: ContactTarget['type'], item: RoomPost | RoommatePost): void {
    const user = this.requireUser();
    if (!user || !item.id) {
      return;
    }
    const ownerId = type === 'room' ? (item as RoomPost).ownerId : (item as RoommatePost).userId;
    if (ownerId === user.id) {
      this.error.set('You cannot send a request to your own post.');
      return;
    }
    this.contactForm.reset({ message: '' });
    this.contact.set({ type, id: item.id, title: item.title, ownerId });
  }

  closeContact(): void {
    this.contact.set(null);
  }

  sendContact(): void {
    const user = this.requireUser();
    const target = this.contact();
    if (!user || !target || !this.isFormValid(this.contactForm)) {
      return;
    }

    this.loading.set(true);
    this.clearMessages();
    const message = this.contactForm.getRawValue().message.trim();
    const request: Observable<unknown> = target.type === 'room'
      ? this.http.post<RoomInterestRequest>(`${this.apiBase}/api/rooms/${target.id}/interest/${user.id}`, { message }, { headers: this.authHeaders() })
      : this.http.post(`${this.apiBase}/roommate/request/${target.id}/${user.id}`, { message }, { headers: this.authHeaders(), responseType: 'text' });

    request.subscribe({
      next: () => {
        this.success.set(target.type === 'room' ? 'Room request sent.' : 'Roommate request sent.');
        this.loading.set(false);
        this.contact.set(null);
      },
      error: (error) => {
        this.loading.set(false);
        this.setError('Request could not be sent', error);
      }
    });
  }

  deleteRoom(room: RoomPost): void {
    const user = this.requireUser();
    if (!user || !room.id || !this.confirmDelete('room listing')) {
      return;
    }
    this.http.delete(`${this.apiBase}/api/rooms/${room.id}/${user.id}`, { headers: this.authHeaders(), responseType: 'text' }).subscribe({
      next: () => {
        this.success.set('Room listing deleted.');
        this.loadRooms();
      },
      error: (error) => this.setError('Room could not be deleted', error)
    });
  }

  deleteProperty(property: PropertyPost): void {
    if (!this.requireUser() || !property.id || !this.confirmDelete('property listing')) {
      return;
    }
    this.http.delete(`${this.apiBase}/api/properties/${property.id}`, { headers: this.authHeaders(), responseType: 'text' }).subscribe({
      next: () => {
        this.success.set('Property listing deleted.');
        this.loadProperties();
      },
      error: (error) => this.setError('Property could not be deleted', error)
    });
  }

  deleteRoommatePost(post: RoommatePost): void {
    if (!this.requireUser() || !post.id || !this.confirmDelete('roommate post')) {
      return;
    }
    this.http.delete(`${this.apiBase}/roommate/${post.id}`, { headers: this.authHeaders(), responseType: 'text' }).subscribe({
      next: () => {
        this.success.set('Roommate post deleted.');
        this.loadRoommates();
      },
      error: (error) => this.setError('Roommate post could not be deleted', error)
    });
  }

  loadRoomRequests(room: RoomPost): void {
    if (!room.id || !this.canManage(room.ownerId)) {
      return;
    }
    this.lastRoomRequestId = room.id;
    this.lastRoommateRequestId = null;
    this.roommateRequests.set([]);
    this.selectedRequestTitle.set(`Requests for ${room.title}`);
    this.requestsOpen.set(true);
    this.http.get<RoomInterestRequest[]>(`${this.apiBase}/api/rooms/${room.id}/requests`, { headers: this.authHeaders() }).subscribe({
      next: (requests) => this.roomRequests.set(requests ?? []),
      error: (error) => this.setError('Room requests could not be loaded', error)
    });
  }

  loadRoommateRequests(post: RoommatePost): void {
    if (!post.id || !this.canManage(post.userId)) {
      return;
    }
    this.lastRoommateRequestId = post.id;
    this.lastRoomRequestId = null;
    this.roomRequests.set([]);
    this.selectedRequestTitle.set(`Requests for ${post.title}`);
    this.requestsOpen.set(true);
    this.http.get<RoommateRequest[]>(`${this.apiBase}/roommate/request/post/${post.id}`, { headers: this.authHeaders() }).subscribe({
      next: (requests) => this.roommateRequests.set(requests ?? []),
      error: (error) => this.setError('Roommate requests could not be loaded', error)
    });
  }

  closeRequests(): void {
    this.requestsOpen.set(false);
    this.roomRequests.set([]);
    this.roommateRequests.set([]);
  }

  acceptRoomRequest(request: RoomInterestRequest): void {
    this.updateRoomRequest(request, 'accept');
  }

  rejectRoomRequest(request: RoomInterestRequest): void {
    this.updateRoomRequest(request, 'reject');
  }

  acceptRoommateRequest(request: RoommateRequest): void {
    this.updateRoommateRequest(request, 'accept');
  }

  rejectRoommateRequest(request: RoommateRequest): void {
    this.updateRoommateRequest(request, 'reject');
  }

  canManage(ownerId?: number): boolean {
    const user = this.auth();
    return !!user && (user.role === 'ADMIN' || user.id === ownerId);
  }

  imageFor(images?: string[]): string {
    return images?.find(Boolean) ?? this.fallbackImage;
  }

  onImageError(event: Event): void {
    const image = event.target as HTMLImageElement;
    if (image.src !== this.fallbackImage) {
      image.src = this.fallbackImage;
    }
  }

  money(value?: number): string {
    return `Rs. ${Number(value ?? 0).toLocaleString('en-IN')}`;
  }

  date(value?: string): string {
    if (!value) {
      return '';
    }
    return new Intl.DateTimeFormat('en-IN', { day: 'numeric', month: 'short', year: 'numeric' }).format(new Date(value));
  }

  private updateRoomRequest(request: RoomInterestRequest, action: 'accept' | 'reject'): void {
    if (!request.id) {
      return;
    }
    this.http.put(`${this.apiBase}/api/rooms/request/${request.id}/${action}`, {}, { headers: this.authHeaders(), responseType: 'text' }).subscribe({
      next: () => {
        this.success.set(`Room request ${action}ed.`);
        if (this.lastRoomRequestId) {
          const room = this.rooms().find((item) => item.id === this.lastRoomRequestId);
          if (room) {
            this.loadRoomRequests(room);
          }
        }
      },
      error: (error) => this.setError('Room request could not be updated', error)
    });
  }

  private updateRoommateRequest(request: RoommateRequest, action: 'accept' | 'reject'): void {
    if (!request.id) {
      return;
    }
    this.http.put(`${this.apiBase}/roommate/request/${request.id}/${action}`, {}, { headers: this.authHeaders(), responseType: 'text' }).subscribe({
      next: () => {
        this.success.set(`Roommate request ${action}ed.`);
        if (this.lastRoommateRequestId) {
          const post = this.roommates().find((item) => item.id === this.lastRoommateRequestId);
          if (post) {
            this.loadRoommateRequests(post);
          }
        }
      },
      error: (error) => this.setError('Roommate request could not be updated', error)
    });
  }

  private filesFromEvent(event: Event): File[] {
    const input = event.target as HTMLInputElement;
    return Array.from(input.files ?? []).slice(0, 8);
  }

  private multipart<T>(partName: string, payload: T, filePartName: string, files: File[]): FormData {
    const formData = new FormData();
    formData.append(partName, new Blob([JSON.stringify(payload)], { type: 'application/json' }));
    files.forEach((file) => formData.append(filePartName, file));
    return formData;
  }

  private authHeaders(): HttpHeaders {
    const token = this.auth()?.token;
    return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
  }

  private requireUser(): LoginResponse | null {
    const user = this.auth();
    if (!user) {
      this.openAuth('login');
      this.error.set('Sign in to continue.');
      return null;
    }
    return user;
  }

  private ensureOwnerRole(): boolean {
    if (!this.isOwner()) {
      this.error.set('Only OWNER or ADMIN accounts can list rental spaces.');
      return false;
    }
    return true;
  }

  private isFormValid(form: FormGroup): boolean {
    if (form.valid) {
      return true;
    }
    form.markAllAsTouched();
    this.error.set('Please complete the highlighted fields.');
    return false;
  }

  private confirmDelete(label: string): boolean {
    return window.confirm(`Delete this ${label}? This cannot be undone.`);
  }

  private clearMessages(): void {
    this.error.set('');
    this.success.set('');
  }

  private setError(prefix: string, error: unknown, clearLoading = false): void {
    if (clearLoading) {
      this.listingsLoading.set(false);
    }
    const detail = error instanceof HttpErrorResponse ? this.readHttpError(error) : 'Unexpected error';
    this.error.set(`${prefix}: ${detail}`);
  }

  private readHttpError(error: HttpErrorResponse): string {
    if (typeof error.error === 'string' && error.error.trim()) {
      return error.error;
    }
    if (error.error?.message) {
      return error.error.message;
    }
    if (error.error && typeof error.error === 'object') {
      return Object.values(error.error).join(', ');
    }
    return error.message;
  }

  private sortNewest<T extends { createdAt?: string }>(items: T[]): T[] {
    return [...items].sort((first, second) => {
      const firstDate = first.createdAt ? new Date(first.createdAt).getTime() : 0;
      const secondDate = second.createdAt ? new Date(second.createdAt).getTime() : 0;
      return secondDate - firstDate;
    });
  }

  private readApiBase(): string {
    if (typeof window !== 'undefined') {
      const configured = (window as Window & { RENT_IT_API_BASE?: string }).RENT_IT_API_BASE;
      if (configured) {
        return configured.replace(/\/$/, '');
      }
    }
    return 'http://localhost:8086';
  }

  private readSession(): LoginResponse | null {
    if (typeof localStorage === 'undefined') {
      return null;
    }
    try {
      const raw = localStorage.getItem(this.sessionKey);
      return raw ? JSON.parse(raw) as LoginResponse : null;
    } catch {
      return null;
    }
  }
}
