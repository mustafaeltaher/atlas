import { Injectable, signal, effect } from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class ThemeService {
    private readonly THEME_KEY = 'atlas-theme';

    isDarkMode = signal<boolean>(this.getInitialTheme());

    constructor() {
        effect(() => {
            this.applyTheme(this.isDarkMode());
        });
    }

    private getInitialTheme(): boolean {
        const saved = localStorage.getItem(this.THEME_KEY);
        if (saved !== null) {
            return saved === 'dark';
        }
        // Default to dark mode (current design)
        return true;
    }

    private applyTheme(isDark: boolean): void {
        document.body.classList.remove('light-theme', 'dark-theme');
        document.body.classList.add(isDark ? 'dark-theme' : 'light-theme');
        localStorage.setItem(this.THEME_KEY, isDark ? 'dark' : 'light');
    }

    toggleTheme(): void {
        this.isDarkMode.set(!this.isDarkMode());
    }

    setDarkMode(isDark: boolean): void {
        this.isDarkMode.set(isDark);
    }
}
