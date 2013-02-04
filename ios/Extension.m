#import "FlashRuntimeExtensions.h"
#import <Heyzap/Heyzap.h>

static FREObject Hayzap_load(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[])
{
    NSLog(@"Entering load()");
    
    if (argc != 3)
    {
        NSLog(@"Invalid amount of args");
        return NULL;
    }
    
    uint32_t length;
    const uint8_t *appStoreId, *appURL;
    if (FREGetObjectAsUTF8(argv[1], &length, &appStoreId) != FRE_OK)
    {
        NSLog(@"FREGetObjectAsUTF8 failed");
        return NULL;
    }
    
    if (FREGetObjectAsUTF8(argv[2], &length, &appURL) != FRE_OK)
    {
        NSLog(@"FREGetObjectAsUTF8 failed");
        return NULL;
    }

    [HeyzapSDK startHeyzapWithAppId:[NSString stringWithUTF8String:(char*)appStoreId] andAppURL:[NSURL URLWithString: [NSString stringWithUTF8String:(char*)appURL]] andShowPopup:YES];
    
    return NULL;
}

static FREObject Hayzap_checkin(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[])
{
    NSLog(@"Entering checkin()");
    
    if (argc != 1)
    {
        NSLog(@"Invalid amount of args");
        return NULL;
    }
    
    uint32_t length;
    const uint8_t *checkinText;
    if (FREGetObjectAsUTF8(argv[0], &length, &checkinText) != FRE_OK)
    {
        NSLog(@"FREGetObjectAsUTF8 failed");
        return NULL;
    }
    
    [[HeyzapSDK sharedSDK] checkinWithMessage: [NSString stringWithUTF8String:(char*)checkinText]];
    
    return NULL;
}

static FREObject Hayzap_isSupported(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[])
{
    NSLog(@"Entering isSupported()");
    
    FREObject object;
    if (FRENewObjectFromBool(HeyzapSDK.isSupported, &object) != FRE_OK)
    {
        NSLog(@"FRENewObjectFromBool failed");
        return NULL;
    }    
    return object;
}

// The context initializer is called when the runtime creates the extension context instance.
static void ContextInitializer(void* extData, const uint8_t* ctxType, FREContext ctx, uint32_t* numFunctionsToTest, const FRENamedFunction** functionsToSet) 
{	
    NSLog(@"Entering ContextInitializer()");
    
	*numFunctionsToTest = 3;
	FRENamedFunction* func = (FRENamedFunction*)malloc(sizeof(FRENamedFunction) * *numFunctionsToTest);
    
	func[0].name = (const uint8_t*)"load";
	func[0].functionData = NULL;
	func[0].function = &Hayzap_load;

    func[1].name = (const uint8_t*)"checkin";
	func[1].functionData = NULL;
	func[1].function = &Hayzap_checkin;

    func[2].name = (const uint8_t*)"isSupported";
	func[2].functionData = NULL;
	func[2].function = &Hayzap_isSupported;
    
	*functionsToSet = func;
    
    NSLog(@"Exiting ContextInitializer()");
}

// The context finalizer is called when the extension's ActionScript code
// calls the ExtensionContext instance's dispose() method.
// If the AIR runtime garbage collector disposes of the ExtensionContext instance, the runtime also calls
// ContextFinalizer().
static void ContextFinalizer(FREContext ctx) {
	
    NSLog(@"Entering ContextFinalizer()");
}

// The extension initializer is called the first time the ActionScript side of the extension
// calls ExtensionContext.createExtensionContext() for any context.
void HeyzapExtInitializer(void** extDataToSet, FREContextInitializer* ctxInitializerToSet, FREContextFinalizer* ctxFinalizerToSet) 
{
    NSLog(@"Entering ExtInitializer()");
    
	*extDataToSet = NULL;
	*ctxInitializerToSet = &ContextInitializer;
	*ctxFinalizerToSet = &ContextFinalizer;
    
    NSLog(@"Exiting ExtInitializer()");
} 

// The extension finalizer is called when the runtime unloads the extension. However, it is not always called.
void HeyzapExtFinalizer(void* extData) 
{
    NSLog(@"Entering ExtFinalizer()");
}
