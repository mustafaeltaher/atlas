# Angular Material Month Picker Setup Guide

**⚠️ DEPRECATED - NOT IMPLEMENTED**

This document was created during the planning phase but **Angular Material Date Picker was NOT used** in the final implementation due to severe UX issues (hanging, displaying days instead of months, couldn't close properly).

**Actual Implementation**: Custom month picker component (MonthPickerComponent) - see [implementation-notes.md](./implementation-notes.md) for details.

---

**Feature**: 002-allocation-month-year-filter
**Date**: 2026-02-23
**UI Library**: ~~Angular Material 17~~ Custom Component
**Status**: Deprecated (kept for historical reference)

---

## Visual Preview: Material Month Picker

### What It Will Look Like

#### Closed State
```
┌────────────────────────────────────┐
│ Month                              │
│ February 2026                   📅 │ ← Material outlined input
└────────────────────────────────────┘
```

#### Open State (Calendar Popup)
```
                    ┌─────────────────────────────┐
                    │    ◄  2026  ►              │ ← Year navigation
                    ├─────────────────────────────┤
                    │                             │
                    │  Jan    Feb    Mar    Apr   │ ← Month grid
                    │  May    Jun    Jul    Aug   │   (3 columns)
                    │  Sep    Oct    Nov    Dec   │
                    │                             │
                    │       [February]            │ ← Selected (filled)
                    │                             │
                    └─────────────────────────────┘
```

**Key Features**:
- ✅ Same UI in Chrome, Safari, Firefox, Edge
- ✅ Smooth animations (fade in/out)
- ✅ Hover effects on months
- ✅ Keyboard navigation (arrows, Enter, Esc)
- ✅ Accessible (ARIA labels, screen reader support)
- ✅ Themeable (can match brand colors)

---

## Step-by-Step Installation

### Step 1: Install Angular Material

```bash
cd /Users/mustafaabdelhalim/Downloads/github/atlas/frontend

# Install Material + dependencies
npm install @angular/material@^17.0.0 @angular/cdk@^17.0.0
```

**Expected Output**:
```
+ @angular/material@17.x.x
+ @angular/cdk@17.x.x
```

**Note**: `@angular/animations` is already installed (required dependency).

---

### Step 2: Add Material Theme (Global Styles)

**File**: `frontend/src/styles.css` (or `styles.scss`)

Add at the **top** of the file:

```css
/* Angular Material Core Theme */
@import '@angular/material/prebuilt-themes/indigo-pink.css';

/* OR for dark mode support: */
/* @import '@angular/material/prebuilt-themes/deeppurple-amber.css'; */

/* Material Icons (if not already included) */
@import 'https://fonts.googleapis.com/icon?family=Material+Icons';
```

**Alternative Themes**:
- `indigo-pink.css` - Default (blue primary, pink accent)
- `deeppurple-amber.css` - Dark mode friendly
- `pink-bluegrey.css` - Pink primary
- `purple-green.css` - Purple primary

**Custom Theme** (optional, for advanced users):
```scss
// Define custom colors to match Atlas brand
@use '@angular/material' as mat;

$atlas-primary: mat.define-palette(mat.$blue-palette, 500);
$atlas-accent: mat.define-palette(mat.$amber-palette, A200);
$atlas-theme: mat.define-light-theme((
  color: (primary: $atlas-primary, accent: $atlas-accent)
));

@include mat.all-component-themes($atlas-theme);
```

---

### Step 3: Update AllocationsComponent Imports

**File**: `frontend/src/app/pages/allocations/allocations.component.ts`

**Add these imports at the top**:

```typescript
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatInputModule } from '@angular/material/input';
import { MatNativeDateModule } from '@angular/material/core';
import { MatFormFieldModule } from '@angular/material/form-field';
```

**Update the `@Component` decorator**:

```typescript
@Component({
  selector: 'app-allocations',
  standalone: true,
  imports: [
    CommonModule,
    SidebarComponent,
    HeaderComponent,
    FormsModule,
    // ADD THESE MATERIAL IMPORTS:
    MatDatepickerModule,
    MatInputModule,
    MatNativeDateModule,
    MatFormFieldModule
  ],
  template: `...`,
  styles: [`...`]
})
```

---

### Step 4: Add Component Methods

**File**: `frontend/src/app/pages/allocations/allocations.component.ts`

**Add these methods to the class**:

```typescript
export class AllocationsComponent implements OnInit {
  // ... existing properties

  // NEW: Month/Year filter state (already added from earlier)
  selectedYear = signal<number>(new Date().getFullYear());
  selectedMonth = signal<number>(new Date().getMonth() + 1); // 1-indexed

  // NEW: Helper method to convert signals to Date object for Material
  getMonthYearDate(): Date {
    return new Date(this.selectedYear(), this.selectedMonth() - 1, 1);
  }

  // NEW: Handler when user selects month from Material calendar
  setMonthAndYear(date: Date, datepicker: any): void {
    this.selectedYear.set(date.getFullYear());
    this.selectedMonth.set(date.getMonth() + 1); // Convert 0-indexed to 1-indexed
    datepicker.close(); // Close calendar after selection
    this.onMonthYearChange();
  }

  // NEW: Optional handler for direct input changes (if user types)
  onMonthYearDateChange(event: any): void {
    const date = event.value as Date;
    if (date) {
      this.selectedYear.set(date.getFullYear());
      this.selectedMonth.set(date.getMonth() + 1);
      this.onMonthYearChange();
    }
  }

  // NEW: Handle month/year change (reset pagination, reload data)
  onMonthYearChange(): void {
    this.currentPage.set(0); // Reset to first page
    this.loadAllocations();
    this.loadManagers();
    this.loadAllocationTypes();
  }

  // ... rest of component
}
```

---

### Step 5: Update Template (HTML)

**File**: `frontend/src/app/pages/allocations/allocations.component.ts` (template section)

**Find the filters bar** (around line 39) and update:

```typescript
template: `
  <!-- ... existing header ... -->

  <!-- Search and Filters -->
  <div class="filters-bar">
    <!-- 1. Search box (existing) -->
    <div class="search-box">
      <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="11" cy="11" r="8"></circle>
        <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
      </svg>
      <input type="text" placeholder="Search by name or email..." [(ngModel)]="searchTerm" (input)="onSearch()">
    </div>

    <!-- 2. NEW: Month/Year Picker (Material) -->
    <mat-form-field class="filter-select month-picker" appearance="outline">
      <mat-label>Month</mat-label>
      <input matInput
             [matDatepicker]="monthPicker"
             [value]="getMonthYearDate()"
             (dateChange)="onMonthYearDateChange($event)"
             readonly>
      <mat-datepicker-toggle matIconSuffix [for]="monthPicker"></mat-datepicker-toggle>
      <mat-datepicker #monthPicker
                      startView="year"
                      (monthSelected)="setMonthAndYear($event, monthPicker)">
      </mat-datepicker>
    </mat-form-field>

    <!-- 3. Allocation Type dropdown (existing) -->
    <select class="filter-select" [(ngModel)]="allocationTypeFilter" (change)="onFilter()">
      <option value="">All Types</option>
      @for (type of allocationTypes(); track type) {
        <option [value]="type">{{ type | titlecase }}</option>
      }
    </select>

    <!-- 4. Manager searchable dropdown (existing) -->
    <div class="searchable-select filter-select-wrap">
      <!-- ... existing manager dropdown code ... -->
    </div>
  </div>

  <!-- ... rest of template ... -->
`,
```

---

### Step 6: Add Component Styles

**File**: `frontend/src/app/pages/allocations/allocations.component.ts` (styles section)

**Add to the existing styles array**:

```typescript
styles: [`
  /* ... existing styles ... */

  /* NEW: Material Month Picker Styling */
  .month-picker {
    min-width: 180px;
    max-width: 200px;
  }

  /* Match Material field height to other filters */
  .month-picker ::ng-deep .mat-mdc-text-field-wrapper {
    height: 40px;
    background: var(--bg-card);
    border-radius: 8px;
  }

  .month-picker ::ng-deep .mat-mdc-form-field-flex {
    height: 40px;
    align-items: center;
  }

  .month-picker ::ng-deep .mat-mdc-form-field-infix {
    padding: 0;
    border: none;
  }

  /* Material label styling */
  .month-picker ::ng-deep .mat-mdc-floating-label {
    color: var(--text-muted);
    font-size: 14px;
  }

  /* Remove Material's default outline (we have our own border) */
  .month-picker ::ng-deep .mat-mdc-notch-piece {
    border: none !important;
  }

  /* Calendar icon button */
  .month-picker ::ng-deep .mat-datepicker-toggle {
    color: var(--text-muted);
  }

  .month-picker ::ng-deep .mat-datepicker-toggle:hover {
    color: var(--primary);
  }

  /* Input text color */
  .month-picker ::ng-deep input {
    color: var(--text);
    font-size: 14px;
    cursor: pointer;
  }

  /* Focus state */
  .month-picker ::ng-deep .mat-focused .mat-mdc-text-field-wrapper {
    border-color: var(--primary);
  }
`]
```

---

## Material Calendar Customization (Optional)

### Custom Calendar Theme (if default colors don't match)

**File**: `frontend/src/styles.css`

```css
/* Override Material calendar colors */
.mat-calendar {
  font-family: inherit;
}

.mat-calendar-body-selected {
  background-color: var(--primary) !important;
  color: white !important;
}

.mat-calendar-body-cell:hover .mat-calendar-body-cell-content {
  background-color: rgba(62, 146, 204, 0.1);
}

.mat-calendar-header {
  background: var(--bg-card);
  color: var(--text);
}

.mat-calendar-arrow {
  fill: var(--text);
}
```

---

## Testing the Material Month Picker

### Manual Test Steps

1. **Start the frontend**:
   ```bash
   cd frontend
   npm start
   ```

2. **Navigate to allocations page**: `http://localhost:4200/allocations`

3. **Verify month picker appearance**:
   - [ ] Month picker displays "Month" label
   - [ ] Shows current month (e.g., "February 2026")
   - [ ] Has calendar icon on the right
   - [ ] Matches height/style of other filters

4. **Click the month picker** (input field or calendar icon):
   - [ ] Calendar popup appears
   - [ ] Shows year with left/right arrows at top
   - [ ] Shows 12 months in 3x4 grid
   - [ ] Current month is highlighted/filled

5. **Navigate years**:
   - [ ] Click left arrow → year decreases (2025, 2024, etc.)
   - [ ] Click right arrow → year increases (2027, 2028, etc.)

6. **Select a different month**:
   - [ ] Click on "June" → calendar closes automatically
   - [ ] Input now shows "June 2026"
   - [ ] Allocations table updates
   - [ ] Pagination resets to page 1

7. **Keyboard navigation**:
   - [ ] Click month picker to open
   - [ ] Press arrow keys → navigates between months
   - [ ] Press Enter → selects highlighted month, closes calendar
   - [ ] Press Esc → closes calendar without selection

8. **Cross-browser test**:
   - [ ] Chrome: Calendar looks correct
   - [ ] Safari: Calendar looks correct (same as Chrome!)
   - [ ] Firefox: Calendar looks correct
   - [ ] Edge: Calendar looks correct

---

## Troubleshooting

### Issue: "No provider for DateAdapter"

**Error**:
```
ERROR NullInjectorError: No provider for DateAdapter!
```

**Fix**: Ensure `MatNativeDateModule` is imported:
```typescript
imports: [
  // ... other imports
  MatNativeDateModule, // <-- Add this
]
```

---

### Issue: Calendar icon not showing

**Error**: Calendar toggle shows empty box instead of icon

**Fix 1**: Import Material Icons in `frontend/src/index.html`:
```html
<head>
  <!-- ... other tags -->
  <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
</head>
```

**Fix 2**: Or add to `frontend/src/styles.css`:
```css
@import url('https://fonts.googleapis.com/icon?family=Material+Icons');
```

---

### Issue: Calendar is not opening when input is clicked

**Fix**: Ensure `matDatepicker` directive is correctly referenced:
```html
<input matInput
       [matDatepicker]="monthPicker"  <!-- Matches #monthPicker below -->
       ...>
<mat-datepicker #monthPicker ...></mat-datepicker>  <!-- Same reference -->
```

---

### Issue: Month not updating when selected

**Fix**: Ensure `monthSelected` event is bound correctly:
```html
<mat-datepicker #monthPicker
                startView="year"
                (monthSelected)="setMonthAndYear($event, monthPicker)">
                              ↑ Make sure this method exists in component
</mat-datepicker>
```

---

### Issue: Calendar shows days instead of months

**Fix**: Set `startView="year"` (not `"month"`):
```html
<mat-datepicker #monthPicker
                startView="year">  <!-- Shows year → month grid -->
</mat-datepicker>
```

---

## Summary: What You Get

✅ **Unified UI**: Same calendar appearance in all browsers
✅ **Professional Look**: Material Design with smooth animations
✅ **Accessible**: ARIA labels, keyboard navigation, screen reader support
✅ **Customizable**: Theme colors, fonts, styles via CSS
✅ **Consistent**: Matches Material design patterns if used elsewhere

**Installation Time**: ~15 minutes
**Implementation Time**: ~30 minutes
**Bundle Size Impact**: +150KB (Material core + datepicker components)

---

## Next Steps

1. ✅ Install Material: `npm install @angular/material @angular/cdk`
2. ✅ Add theme to `styles.css`
3. ✅ Update component imports
4. ✅ Add component methods
5. ✅ Update template with Material month picker
6. ✅ Add styles
7. ✅ Test in browser

**Ready to implement!** 🚀
