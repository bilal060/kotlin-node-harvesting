import React from 'react';
import errorLogger from '../lib/errorLogger';

/**
 * React Error Boundary component
 * Catches JavaScript errors anywhere in the child component tree
 * and reports them to the error logging service
 */
class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null, errorInfo: null };
  }

  static getDerivedStateFromError(error) {
    // Update state so the next render will show the fallback UI
    return { hasError: true };
  }

  componentDidCatch(error, errorInfo) {
    // Log the error to our error reporting service
    errorLogger.reportError(
      `React component error: ${error.message}`,
      error,
      errorLogger.ErrorLevel.ERROR,
      errorLogger.ErrorSource.UI_ERROR,
      {
        componentStack: errorInfo.componentStack,
        errorName: error.name,
        errorMessage: error.message,
        url: window.location.href
      },
      true // Send immediately
    );

    // Update state with error details
    this.setState({
      error: error,
      errorInfo: errorInfo
    });

    // Log to console for development
    console.error('React Error Boundary caught an error:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      // Custom fallback UI
      return this.props.fallback ? (
        this.props.fallback(this.state.error, this.state.errorInfo)
      ) : (
        <div className="error-boundary-fallback">
          <div className="error-container">
            <h2>🚨 Something went wrong</h2>
            <p>We&apos;ve encountered an unexpected error. Our team has been notified.</p>
            <button 
              onClick={() => {
                this.setState({ hasError: false, error: null, errorInfo: null });
                window.location.reload();
              }}
              className="retry-button"
            >
              Try Again
            </button>
            {process.env.NODE_ENV === 'development' && (
              <details className="error-details">
                <summary>Error Details (Development)</summary>
                <pre>{this.state.error && this.state.error.toString()}</pre>
                <pre>{this.state.errorInfo.componentStack}</pre>
              </details>
            )}
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

/**
 * Hook for functional components to report errors
 */
export const useErrorReporting = () => {
  const reportError = React.useCallback((error, additionalData = {}) => {
    errorLogger.reportError(
      `Functional component error: ${error.message}`,
      error,
      errorLogger.ErrorLevel.ERROR,
      errorLogger.ErrorSource.UI_ERROR,
      {
        ...additionalData,
        hook: 'useErrorReporting'
      }
    );
  }, []);

  return { reportError };
};

/**
 * Higher-order component for error reporting
 */
export const withErrorReporting = (WrappedComponent, componentName = 'Unknown') => {
  return function WithErrorReportingComponent(props) {
    const { reportError } = useErrorReporting();

    const handleError = React.useCallback((error, additionalData = {}) => {
      reportError(error, {
        ...additionalData,
        componentName,
        props: Object.keys(props)
      });
    }, [reportError, props]);

    return (
      <ErrorBoundary
        fallback={(error, errorInfo) => (
          <div className="component-error-fallback">
            <h3>Component Error: {componentName}</h3>
            <p>An error occurred in this component. Please try refreshing the page.</p>
            {process.env.NODE_ENV === 'development' && (
              <details>
                <summary>Error Details</summary>
                <pre>{error.toString()}</pre>
                <pre>{errorInfo.componentStack}</pre>
              </details>
            )}
          </div>
        )}
      >
        <WrappedComponent {...props} onError={handleError} />
      </ErrorBoundary>
    );
  };
};

export default ErrorBoundary; 