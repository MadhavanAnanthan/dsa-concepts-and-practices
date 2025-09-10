## 1. React Components and Their Advantages

### What are React Components?

- **Components** are independent, reusable chunks of code that return JSX (HTML-like code) and manage their own logic and UI rendering.[^1][^2][^3]
- Components can be either **functional** (recommended) or **class**-based, but functional components with Hooks are the new standard.[^2][^1]
- Example of a functional component:

```jsx
function Greeting() {
  return <h1>Hello, welcome to React!</h1>;
}
```


### Advantages of Components

- **Reusability:** Build once, use anywhere for consistent UI.[^4][^2]
- **Separation of Concerns:** Logic and presentation are modular and maintainable.
- **Easy Maintenance:** Isolated changes don’t break unrelated areas.
- **Testability:** Independent units are easier to test.[^4][^2]


### What are Pre-built Components?

- Pre-built components are ready-made UI elements provided by React libraries (like React Bootstrap, Material UI, PrimeReact), e.g., buttons, forms, tables, tooltips.[^5][^6][^7]
- React itself ships built-ins like `<Fragment>`, `<StrictMode>`, and `<Suspense>` for specific purposes.[^5]


### How to Create Custom Components (with Example)

- Custom components are your own React functions or classes.

```jsx
// App.jsx
import Card from './Card.jsx';

function App() {
  return (
    <Card>Hello!</Card>
  );
}
```
```jsx
// Card.jsx
function Card(props) {
  return <div className="card">{props.children}</div>;
}
export default Card;

```


### Power of JSX or TSX Extensions

- **JSX**: A syntax extension to JavaScript that lets you write HTML-like code in JS files.
- **TSX**: Like JSX, but for TypeScript, enabling static type checking and IDE autocompletion.
- JSX/TSX combines logic and UI for clarity, enables conditional rendering, and leverages all JS features in markup.[^8]


### How React Handles Components \& the "Component Tree"

- React represents UI as a **component tree**, where each component is a node (parent or child).[^9][^10]
- The **root** is often `App`, wrapping the entire app. Each nested component forms branches and leaves.
- Data flows down via props; visualizing this hierarchy is key for debugging and optimization.[^10][^9]

***

## 2. Using \& Outputting Dynamic Values

### How to Use Dynamic Value Mapping

- Use the `map()` function to iterate arrays and render dynamic lists or elements.

```jsx
const fruits = ["Apple", "Banana", "Cherry"];
return (
  <ul>
    {fruits.map((item, idx) => <li key={idx}>{item}</li>)}
  </ul>
);
```

- Props or state supply dynamic data to be rendered.[^11][^12]


### Outputting Dynamic Values [Core Concept]

- Anything inside `{}` in JSX will be evaluated as JavaScript, letting you output variables, expressions, or state.

```jsx
const name = "React";
return <h1>Hello, {name}!</h1>;
```

- Update state/props to see the UI change automatically.[^13]


### Setting HTML Attributes Dynamically \& Loading Image Files

- You can dynamically set any HTML attribute in JSX, including `src`, `alt`, `id`, etc.

```jsx
function Avatar({ imgSrc, userId }) {
  return <img src={imgSrc} data-user-id={userId} alt="profile"/>
}
```

    - For HTML5 "data-" attributes, use camelCase or string syntax for props with hyphens.[^14]
    - For dynamic image imports (bundler required):

```jsx
import imgA from './assets/imgA.png';
<img src={imgA} alt="A" />
// or use require.context/web dynamic import for many images[^18][^21][^23]
```

- Spread operator can be used for dynamically assigning multiple attributes:

```jsx
<input {...otherProps} />
```


***

## 3. What is Meant by "props" in React?

### Understanding Props

- **Props** (properties) are read-only inputs passed to a component for configuration, similar to function arguments.[^3][^1][^2]
- Props enable **reusability** by allowing components to behave differently depending on the passed values.
- When you pass values to a component (e.g. <Button label="Click me" />), those values are accessible inside the component as props.label.
#### Main Usage Example

```jsx
function App() {
  return (
    <div>
      <Header />
      <main>
        <section id="core-concepts">
          <ul>
            <PropExample imgProp={componentImage} title="Core Concepts" description="Learn the core concepts of React including components, props, state, and lifecycle methods." />
            <PropExample imgProp={componentImage} title="JSX Power" description="Discover why JSX and TSX revolutionize UI building." />
          </ul>
        </section>
      </main>
    </div>
  );
}
```


#### Prop Receiving Component - Before (Functional)

Your code:

```jsx
function PropExample(props) {
  return (
    <li>
      <img src={props.imgProp} />
      <h3>{props.title}</h3>
      <p>{props.description}</p>
    </li>
  );
}
```

**This is perfect for beginners!**

#### Improved (Destructured)

```jsx
function PropExample({ imgProp, title, description }) {
  return (
    <li>
      <img src={imgProp} alt={title} />
      <h3>{title}</h3>
      <p>{description}</p>
    </li>
  );
}
```

- Adds alt attribute for accessibility.
- Uses destructuring for clarity—best practice and easier to maintain.


### Alternative to Props: Passing Data From External File

- Define data objects in a `data.js` file and import them.

```jsx
// data.js
import componentImage from './assets/components.png';
export const CORE_CONCEPTS = [
  {
    imgProp: componentImage,
    title: 'Components',
    description: 'Learn Components.'
  },
  {
    imgProp: componentImage,
    title: 'JSX',
    description: 'JSX blends JavaScript and HTML.'
  }
];
```

    - Import and use via spread operator for cleaner code:

```jsx
import { CORE_CONCEPTS } from './data.js';
function App() {
  return (
    <ul>
      {CORE_CONCEPTS.map((concept, idx) => (
      // based on number of objects defined inside data.js, it will be displayed
        <PropExample key={idx} {...concept} />
      ))}
    </ul>
  );
}
```

- Destructuring in the component simplifies prop usage.

props.children

- Every component automatically receives a special prop called children.
- It represents the content that is nested inside the component’s opening and closing tags.

Example:

```
function Wrapper(props) {
  return <div className="wrapper">{props.children}</div>;
}
// Usage:
<Wrapper>
  <h1>Hello</h1>
  <p>This is inside wrapper</p>
</Wrapper>
```
So unlike normal props (where you explicitly pass values via attributes), children is for passing JSX/content directly inside the component.