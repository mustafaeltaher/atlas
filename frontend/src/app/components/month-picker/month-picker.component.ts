import { Component, EventEmitter, Input, Output, OnInit, OnChanges, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

interface MonthOption {
  value: number; // 1-12
  label: string;
  available: boolean;
}

@Component({
  selector: 'app-month-picker',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="month-picker-container">
      <button type="button"
              class="month-picker-trigger"
              (click)="toggleDropdown()"
              [class.open]="isOpen()">
        <span class="selected-value">{{ getSelectedLabel() }}</span>
        <svg class="arrow" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="6 9 12 15 18 9"></polyline>
        </svg>
      </button>

      @if (isOpen()) {
        <div class="month-picker-dropdown" (click)="$event.stopPropagation()">
          <!-- Year Navigation -->
          <div class="year-header">
            <button type="button" class="year-nav" (click)="previousYear()" [disabled]="loading()">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="15 18 9 12 15 6"></polyline>
              </svg>
            </button>
            <span class="year-label">{{ displayYear() }}</span>
            <button type="button" class="year-nav" (click)="nextYear()" [disabled]="loading()">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="9 18 15 12 9 6"></polyline>
              </svg>
            </button>
          </div>

          <!-- Month Grid -->
          <div class="month-grid">
            @for (month of months; track month.value) {
              <button type="button"
                      class="month-btn"
                      [class.selected]="isSelected(month.value)"
                      [class.unavailable]="!month.available"
                      [disabled]="!month.available || loading()"
                      (click)="selectMonth(month.value)">
                {{ month.label }}
              </button>
            }
          </div>

          @if (loading()) {
            <div class="loading-overlay">Loading...</div>
          }
        </div>
      }
    </div>

    <!-- Click outside to close -->
    @if (isOpen()) {
      <div class="backdrop" (click)="closeDropdown()"></div>
    }
  `,
  styles: [`
    .month-picker-container {
      position: relative;
      display: inline-block;
    }

    .month-picker-trigger {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 8px;
      padding: 0 12px;
      height: 40px;
      min-width: 160px;
      background: var(--bg-card);
      border: 1px solid var(--border);
      border-radius: 8px;
      color: var(--text);
      font-size: 14px;
      cursor: pointer;
      transition: all 0.2s;
      user-select: none;
    }

    .month-picker-trigger:hover {
      border-color: var(--primary);
    }

    .month-picker-trigger:focus {
      outline: none;
      border-color: var(--primary);
      box-shadow: 0 0 0 3px rgba(62, 146, 204, 0.1);
    }

    .month-picker-trigger.open {
      border-color: var(--primary);
    }

    .selected-value {
      flex: 1;
      text-align: left;
    }

    .arrow {
      transition: transform 0.2s;
    }

    .month-picker-trigger.open .arrow {
      transform: rotate(180deg);
    }

    .backdrop {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      z-index: 999;
    }

    .month-picker-dropdown {
      position: absolute;
      top: calc(100% + 4px);
      left: 0;
      min-width: 280px;
      background: var(--bg-card);
      border: 1px solid var(--border);
      border-radius: 8px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      padding: 12px;
      z-index: 1000;
      animation: fadeIn 0.15s ease-out;
    }

    @keyframes fadeIn {
      from {
        opacity: 0;
        transform: translateY(-8px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    .year-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 12px;
      padding-bottom: 12px;
      border-bottom: 1px solid var(--border);
    }

    .year-label {
      font-size: 16px;
      font-weight: 600;
      color: var(--text);
    }

    .year-nav {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 32px;
      height: 32px;
      background: transparent;
      border: none;
      border-radius: 6px;
      color: var(--text);
      cursor: pointer;
      transition: background 0.2s;
    }

    .year-nav:hover:not(:disabled) {
      background: var(--bg-hover);
    }

    .year-nav:disabled {
      opacity: 0.3;
      cursor: not-allowed;
    }

    .month-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 8px;
    }

    .month-btn {
      padding: 10px 8px;
      background: var(--bg-secondary);
      border: 1px solid var(--border);
      border-radius: 6px;
      color: var(--text);
      font-size: 13px;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.2s;
      text-align: center;
    }

    .month-btn:hover:not(:disabled) {
      background: var(--bg-hover);
      border-color: var(--primary);
    }

    .month-btn.selected {
      background: var(--primary);
      border-color: var(--primary);
      color: white;
    }

    .month-btn.unavailable {
      opacity: 0.3;
      cursor: not-allowed;
      background: var(--bg-secondary);
    }

    .month-btn:disabled {
      cursor: not-allowed;
    }

    .loading-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.05);
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 8px;
      font-size: 13px;
      color: var(--text-muted);
    }
  `]
})
export class MonthPickerComponent implements OnInit, OnChanges {
  @Input() selectedYear!: number;
  @Input() selectedMonth!: number; // 1-12
  @Input() availableMonths: string[] = []; // Format: "YYYY-MM"
  @Output() monthChange = new EventEmitter<{ year: number; month: number }>();

  isOpen = signal(false);
  displayYear = signal(new Date().getFullYear());
  loading = signal(false);

  months: MonthOption[] = [
    { value: 1, label: 'Jan', available: true },
    { value: 2, label: 'Feb', available: true },
    { value: 3, label: 'Mar', available: true },
    { value: 4, label: 'Apr', available: true },
    { value: 5, label: 'May', available: true },
    { value: 6, label: 'Jun', available: true },
    { value: 7, label: 'Jul', available: true },
    { value: 8, label: 'Aug', available: true },
    { value: 9, label: 'Sep', available: true },
    { value: 10, label: 'Oct', available: true },
    { value: 11, label: 'Nov', available: true },
    { value: 12, label: 'Dec', available: true }
  ];

  ngOnInit() {
    this.displayYear.set(this.selectedYear);
    this.updateAvailability();
  }

  ngOnChanges() {
    this.updateAvailability();
  }

  updateAvailability() {
    const currentYear = this.displayYear();
    const availableSet = new Set(this.availableMonths);

    this.months = this.months.map(month => ({
      ...month,
      available: availableSet.has(`${currentYear}-${month.value.toString().padStart(2, '0')}`)
    }));
  }

  getSelectedLabel(): string {
    const monthLabel = this.months.find(m => m.value === this.selectedMonth)?.label || 'Unknown';
    return `${monthLabel} ${this.selectedYear}`;
  }

  isSelected(month: number): boolean {
    return this.selectedMonth === month && this.selectedYear === this.displayYear();
  }

  toggleDropdown() {
    this.isOpen.set(!this.isOpen());
    if (this.isOpen()) {
      this.displayYear.set(this.selectedYear);
      this.updateAvailability();
    }
  }

  closeDropdown() {
    this.isOpen.set(false);
  }

  previousYear() {
    this.displayYear.update(y => y - 1);
    this.updateAvailability();
  }

  nextYear() {
    this.displayYear.update(y => y + 1);
    this.updateAvailability();
  }

  selectMonth(month: number) {
    this.monthChange.emit({ year: this.displayYear(), month });
    this.closeDropdown();
  }
}
