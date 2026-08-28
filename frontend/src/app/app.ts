import { CommonModule } from '@angular/common';
import { HttpClient, HttpErrorResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';

type Tab = 'rooms' | 'properties' | 'roommates';

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

  readonly apiBase = 'http://localhost:8086';
  readonly fallbackImage = `${this.apiBase}/uploads/bce13c5d-0abe-4c6b-afc6-3b6df1a6386c_flate1.jpg`;

  readonly auth = signal<LoginResponse | null>(this.readSession());
  readonly activeTab = signal<Tab>('rooms');
  readonly rooms = signal<RoomPost[]>([]);
  readonly properties = signal<PropertyPost[]>([]);
  readonly roommates = signal<RoommatePost[]>([]);
  readonly roomRequests = signal<RoomInterestRequest[]>([]);
  readonly roommateRequests = signal<RoommateRequest[]>([]);
  readonly selectedRequestTitle = signal('Requests');
  readonly loading = signal(false);
  readonly error = signal('');
  readonly success = signal('');

  readonly isOwner = computed(() => {
    const user = this.auth();
    return user?.role === 'OWNER' || user?.role === 'ADMIN';
  });

  readonly loginForm = this.fb.nonNullable.group({
    email: '',
    password: ''
  });

  readonly registerForm = this.fb.nonNullable.group({
    name: '',
    email: '',
    password: '',
    phone: '',
    role: 'USER'
  });

  readonly verifyForm = this.fb.nonNullable.group({
    email: '',
    otp: ''
  });

  readonly searchForm = this.fb.nonNullable.group({
    city: ''
  });

  readonly roomForm = this.fb.nonNullable.group({
    title: '',
    description: '',
    city: '',
    address: '',
    rent: 0,
    capacity: 1
  });

  readonly propertyForm = this.fb.nonNullable.group({
    title: '',
    description: '',
    address: '',
    city: '',
    rent: 0,
    deposit: 0,
    bhkType: '1 BHK'
  });

  readonly roommateForm = this.fb.nonNullable.group({
    title: '',
    description: '',
    city: '',
    rent: '',
    genderPreference: 'Any',
    foodPreference: 'Any',
    smoking: false,
    occupation: ''
  });

  private selectedRoomImages: File[] = [];
  private selectedPropertyImages: File[] = [];
  private lastRoomRequestId: number | null = null;
  private lastRoommateRequestId: number | null = null;

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.loadRooms();
    this.loadProperties();
    this.loadRoommates();
  }

  loadRooms(): void {
    this.http.get<RoomPost[]>(`${this.apiBase}/api/rooms`).subscribe({
      next: (rooms) => this.rooms.set(rooms ?? []),
      error: (error) => this.setError('Rooms could not be loaded', error)
    });
  }

  loadProperties(): void {
    this.http.get<PropertyPost[]>(`${this.apiBase}/api/properties`).subscribe({
      next: (properties) => this.properties.set(properties ?? []),
      error: (error) => this.setError('Properties could not be loaded', error)
    });
  }

  loadRoommates(): void {
    this.http.get<RoommatePost[]>(`${this.apiBase}/roommate/getAllPost`).subscribe({
      next: (roommates) => this.roommates.set(roommates ?? []),
      error: (error) => this.setError('Roommate posts could not be loaded', error)
    });
  }

  searchByCity(): void {
    const city = this.searchForm.getRawValue().city.trim();
    if (!city) {
      this.loadAll();
      return;
    }

    const encodedCity = encodeURIComponent(city);
    this.http.get<RoomPost[]>(`${this.apiBase}/api/rooms/city/${encodedCity}`).subscribe({
      next: (rooms) => this.rooms.set(rooms ?? []),
      error: (error) => this.setError('Room search failed', error)
    });
    this.http.get<PropertyPost[]>(`${this.apiBase}/api/properties/city/${encodedCity}`).subscribe({
      next: (properties) => this.properties.set(properties ?? []),
      error: (error) => this.setError('Property search failed', error)
    });
    this.http.get<RoommatePost[]>(`${this.apiBase}/roommate/getPostByCity/${encodedCity}`).subscribe({
      next: (roommates) => this.roommates.set(roommates ?? []),
      error: (error) => this.setError('Roommate search failed', error)
    });
  }

  clearSearch(): void {
    this.searchForm.reset({ city: '' });
    this.loadAll();
  }

  login(): void {
    this.loading.set(true);
    this.clearMessages();
    this.http.post<LoginResponse>(`${this.apiBase}/auth/login`, this.loginForm.getRawValue()).subscribe({
      next: (response) => {
        this.auth.set(response);
        localStorage.setItem(this.sessionKey, JSON.stringify(response));
        this.success.set(`Signed in as ${response.name}`);
        this.loading.set(false);
      },
      error: (error) => {
        this.loading.set(false);
        this.setError('Login failed', error);
      }
    });
  }

  register(): void {
    this.loading.set(true);
    this.clearMessages();
    const payload: UserDto = this.registerForm.getRawValue();

    this.http.post<UserDto>(`${this.apiBase}/user/register`, payload).subscribe({
      next: (user) => {
        this.verifyForm.patchValue({ email: user.email });
        this.success.set('OTP sent for verification');
        this.loading.set(false);
      },
      error: (error) => {
        this.loading.set(false);
        this.setError('Registration failed', error);
      }
    });
  }

  verifyOtp(): void {
    this.loading.set(true);
    this.clearMessages();
    const raw = this.verifyForm.getRawValue();
    const params = new HttpParams().set('email', raw.email).set('otp', raw.otp);

    this.http.post<UserDto>(`${this.apiBase}/user/verify-otp`, null, { params }).subscribe({
      next: () => {
        this.success.set('Email verified. You can sign in now.');
        this.loginForm.patchValue({ email: raw.email });
        this.loading.set(false);
      },
      error: (error) => {
        this.loading.set(false);
        this.setError('OTP verification failed', error);
      }
    });
  }

  logout(): void {
    this.auth.set(null);
    localStorage.removeItem(this.sessionKey);
    this.success.set('Signed out');
  }

  onRoomImagesSelected(event: Event): void {
    this.selectedRoomImages = this.filesFromEvent(event);
  }

  onPropertyImagesSelected(event: Event): void {
    this.selectedPropertyImages = this.filesFromEvent(event);
  }

  createRoom(): void {
    const user = this.requireUser();
    if (!user || !this.ensureOwnerRole()) {
      return;
    }

    const raw = this.roomForm.getRawValue();
    const payload: RoomPost = {
      title: raw.title,
      description: raw.description,
      city: raw.city,
      address: raw.address,
      rent: Number(raw.rent),
      capacity: Number(raw.capacity),
      available: true
    };

    const formData = this.multipart('room', payload, 'images', this.selectedRoomImages);
    this.loading.set(true);
    this.clearMessages();

    this.http.post<RoomPost>(`${this.apiBase}/api/rooms/${user.id}`, formData, {
      headers: this.authHeaders()
    }).subscribe({
      next: () => {
        this.roomForm.reset({ title: '', description: '', city: '', address: '', rent: 0, capacity: 1 });
        this.selectedRoomImages = [];
        this.success.set('Room posted');
        this.loading.set(false);
        this.loadRooms();
      },
      error: (error) => {
        this.loading.set(false);
        this.setError('Room could not be posted', error);
      }
    });
  }

  createProperty(): void {
    const user = this.requireUser();
    if (!user || !this.ensureOwnerRole()) {
      return;
    }

    const raw = this.propertyForm.getRawValue();
    const payload: PropertyPost = {
      title: raw.title,
      description: raw.description,
      address: raw.address,
      city: raw.city,
      rent: Number(raw.rent),
      deposit: Number(raw.deposit),
      bhkType: raw.bhkType,
      available: true,
      ownerId: user.id
    };

    const formData = this.multipart('property', payload, 'files', this.selectedPropertyImages);
    this.loading.set(true);
    this.clearMessages();

    this.http.post<PropertyPost>(`${this.apiBase}/api/properties/add/${user.id}`, formData, {
      headers: this.authHeaders()
    }).subscribe({
      next: () => {
        this.propertyForm.reset({
          title: '',
          description: '',
          address: '',
          city: '',
          rent: 0,
          deposit: 0,
          bhkType: '1 BHK'
        });
        this.selectedPropertyImages = [];
        this.success.set('Property posted');
        this.loading.set(false);
        this.loadProperties();
      },
      error: (error) => {
        this.loading.set(false);
        this.setError('Property could not be posted', error);
      }
    });
  }

  createRoommatePost(): void {
    const user = this.requireUser();
    if (!user) {
      return;
    }

    this.loading.set(true);
    this.clearMessages();
    this.http.post<RoommatePost>(
      `${this.apiBase}/roommate/createPost/${user.id}`,
      this.roommateForm.getRawValue(),
      { headers: this.authHeaders() }
    ).subscribe({
      next: () => {
        this.roommateForm.reset({
          title: '',
          description: '',
          city: '',
          rent: '',
          genderPreference: 'Any',
          foodPreference: 'Any',
          smoking: false,
          occupation: ''
        });
        this.success.set('Roommate post created');
        this.loading.set(false);
        this.loadRoommates();
      },
      error: (error) => {
        this.loading.set(false);
        this.setError('Roommate post could not be created', error);
      }
    });
  }

  sendRoomInterest(room: RoomPost, message: string): void {
    const user = this.requireUser();
    if (!user || !room.id) {
      return;
    }

    this.clearMessages();
    this.http.post<RoomInterestRequest>(
      `${this.apiBase}/api/rooms/${room.id}/interest/${user.id}`,
      { message },
      { headers: this.authHeaders() }
    ).subscribe({
      next: () => this.success.set('Interest sent to owner'),
      error: (error) => this.setError('Interest could not be sent', error)
    });
  }

  sendRoommateRequest(post: RoommatePost, message: string): void {
    const user = this.requireUser();
    if (!user || !post.id) {
      return;
    }

    this.clearMessages();
    this.http.post(
      `${this.apiBase}/roommate/request/${post.id}/${user.id}`,
      { message },
      { headers: this.authHeaders(), responseType: 'text' }
    ).subscribe({
      next: () => this.success.set('Roommate request sent'),
      error: (error) => this.setError('Roommate request could not be sent', error)
    });
  }

  deleteRoom(room: RoomPost): void {
    const user = this.requireUser();
    if (!user || !room.id) {
      return;
    }

    this.http.delete(`${this.apiBase}/api/rooms/${room.id}/${user.id}`, {
      headers: this.authHeaders(),
      responseType: 'text'
    }).subscribe({
      next: () => {
        this.success.set('Room deleted');
        this.loadRooms();
      },
      error: (error) => this.setError('Room could not be deleted', error)
    });
  }

  deleteProperty(property: PropertyPost): void {
    if (!this.requireUser() || !property.id) {
      return;
    }

    this.http.delete(`${this.apiBase}/api/properties/${property.id}`, {
      headers: this.authHeaders(),
      responseType: 'text'
    }).subscribe({
      next: () => {
        this.success.set('Property deleted');
        this.loadProperties();
      },
      error: (error) => this.setError('Property could not be deleted', error)
    });
  }

  deleteRoommatePost(post: RoommatePost): void {
    if (!this.requireUser() || !post.id) {
      return;
    }

    this.http.delete(`${this.apiBase}/roommate/${post.id}`, {
      headers: this.authHeaders(),
      responseType: 'text'
    }).subscribe({
      next: () => {
        this.success.set('Roommate post deleted');
        this.loadRoommates();
      },
      error: (error) => this.setError('Roommate post could not be deleted', error)
    });
  }

  loadRoomRequests(room: RoomPost): void {
    if (!room.id) {
      return;
    }

    this.lastRoomRequestId = room.id;
    this.lastRoommateRequestId = null;
    this.roommateRequests.set([]);
    this.selectedRequestTitle.set(`Room requests: ${room.title}`);
    this.http.get<RoomInterestRequest[]>(`${this.apiBase}/api/rooms/${room.id}/requests`, {
      headers: this.authHeaders()
    }).subscribe({
      next: (requests) => this.roomRequests.set(requests ?? []),
      error: (error) => this.setError('Room requests could not be loaded', error)
    });
  }

  loadRoommateRequests(post: RoommatePost): void {
    if (!post.id) {
      return;
    }

    this.lastRoommateRequestId = post.id;
    this.lastRoomRequestId = null;
    this.roomRequests.set([]);
    this.selectedRequestTitle.set(`Roommate requests: ${post.title}`);
    this.http.get<RoommateRequest[]>(`${this.apiBase}/roommate/request/post/${post.id}`, {
      headers: this.authHeaders()
    }).subscribe({
      next: (requests) => this.roommateRequests.set(requests ?? []),
      error: (error) => this.setError('Roommate requests could not be loaded', error)
    });
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

  money(value?: number): string {
    if (value == null) {
      return 'Rs. 0';
    }

    return `Rs. ${Number(value).toLocaleString('en-IN')}`;
  }

  private updateRoomRequest(request: RoomInterestRequest, action: 'accept' | 'reject'): void {
    if (!request.id) {
      return;
    }

    this.http.put(`${this.apiBase}/api/rooms/request/${request.id}/${action}`, {}, {
      headers: this.authHeaders(),
      responseType: 'text'
    }).subscribe({
      next: () => {
        this.success.set(`Room request ${action}ed`);
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

    this.http.put(`${this.apiBase}/roommate/request/${request.id}/${action}`, {}, {
      headers: this.authHeaders(),
      responseType: 'text'
    }).subscribe({
      next: () => {
        this.success.set(`Roommate request ${action}ed`);
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
    return Array.from(input.files ?? []);
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
      this.error.set('Sign in first');
      return null;
    }

    return user;
  }

  private ensureOwnerRole(): boolean {
    if (!this.isOwner()) {
      this.error.set('Login as OWNER or ADMIN for rental posts');
      return false;
    }

    return true;
  }

  private clearMessages(): void {
    this.error.set('');
    this.success.set('');
  }

  private setError(prefix: string, error: unknown): void {
    const detail = error instanceof HttpErrorResponse
      ? this.readHttpError(error)
      : 'Unexpected error';
    this.error.set(`${prefix}: ${detail}`);
  }

  private readHttpError(error: HttpErrorResponse): string {
    if (typeof error.error === 'string' && error.error.trim()) {
      return error.error;
    }
    if (error.error?.message) {
      return error.error.message;
    }
    return error.message;
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
