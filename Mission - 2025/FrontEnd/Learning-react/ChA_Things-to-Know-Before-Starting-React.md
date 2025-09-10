# 📘 Things to Know Before Starting React

Before diving into React, it’s important to understand some foundational concepts about how browsers, Node.js, and deployment work.

---

## 1. How UI Renders in the Browser
- Every browser maintains a **DOM (Document Object Model)**.
- The browser always expects an **HTML response**.
- Once the browser receives HTML, it builds the **DOM tree**, which is what users see as the web page.
- CSS styles and JavaScript then update or modify the DOM dynamically.

---

## 2. What is Node.js and NPM? Why Are They Required?
- **Node.js**
    - A JavaScript runtime built on Chrome’s V8 engine.
    - Allows running JavaScript outside the browser (on servers or development machines).
    - Provides additional APIs for working with the OS, files, and network.

- **NPM (Node Package Manager)**
    - Package manager that comes with Node.js.
    - Used to install libraries, frameworks, and tools.
    - Helps in:
        - Installing dependencies (like React, Webpack, Babel).
        - Running local development servers.
        - Building production-ready bundles.

✅ In React projects:
- Node.js runs the build tools.
- NPM/Yarn manages project dependencies.

---

## 3. What Does a Browser Contain?
- **JavaScript Engine** (e.g., V8 in Chrome, SpiderMonkey in Firefox).
- **Browser APIs**, such as:
    - `document` → interact with the DOM.
    - `window` → manage window/tab.
    - `fetch` → make network requests.
- These APIs allow JavaScript to manipulate the web page but **do not provide OS-level access**.

---

## 4. Browser APIs vs Node.js APIs
- **Browser APIs**
    - Work inside the browser.
    - Examples: `document`, `window`, `fetch`, `localStorage`.
    - Control UI, handle user interactions, network requests, etc.
    - Limited to security sandbox (cannot access file system directly).

- **Node.js APIs**
    - Work outside the browser.
    - Examples: `fs` (file system), `http` (create servers), `path`, `os`.
    - Provide OS-level access like reading/writing files, creating directories, handling sockets.
    - Used to build backend servers or development tooling.

---

## 5. How to Deploy JavaScript-Based Projects on a Server
1. **Development Phase**
    - Use Node.js + NPM/Yarn to run build tools.
    - Generate a **production build** (static HTML, CSS, and JavaScript files).

2. **Deployment Phase**
    - Move the build folder (usually `dist/` or `build/`) to your server.
    - Since output is static, any web server (Apache, Nginx, or even a simple Node.js static server) can serve it.
    - When users access the server’s IP/domain, the browser downloads the HTML, CSS, JS → then builds the DOM.

✅ Node.js is **required for development/building**, but **not required to run the final built React app** on the server.

---

## 🚀 Summary
- Browsers expect HTML and build the DOM.
- Node.js + NPM are essential for building and managing modern JS applications.
- Browsers provide **UI-related APIs**, Node.js provides **OS-level APIs**.
- Deployment only needs the static build output — Node.js is not required on the production server unless you’re running a Node backend.
